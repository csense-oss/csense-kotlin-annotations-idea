package csense.kotlin.annotations.idea.inspections

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import csense.kotlin.Function0
import csense.kotlin.annotations.idea.ClassHierarchyAnnotationsCache
import csense.kotlin.annotations.idea.Constants
import csense.kotlin.annotations.idea.bll.getKotlinFqNameString
import csense.kotlin.annotations.idea.psi.resolveAllClassAnnotations
import csense.kotlin.annotations.idea.psi.resolveAllMethodAnnotations
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.references.resolveMainReferenceToDescriptors
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfType
import org.jetbrains.kotlin.resolve.calls.callUtil.getCalleeExpressionIfAny
import org.jetbrains.uast.*

//purpose: to provide simple (non deep) inspection of threading issues (where we look at
// @AnyThread / @InUi threading
// we should also look into
// Android annotations & idea annotations to provide help in theses scenario's as well.
// well the "AnyThread" is any method that is not annotated.
// it should be "worker" / "background" ish something.
// the same for "IO" (which is also bad to do on a UI thread).


class QuickThreadingInspection : AbstractKotlinInspection() {
    //eg look for:
    //launch(Dispatchers.Main){
    //any thread access here
    // }
    //or opposite (where its not main but ui thread is called.

    override fun getDisplayName(): String {
        return "ThreadingInspector"
    }

    override fun getStaticDescription(): String? {
        return """
            
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String? {
        return "more desc ? "
    }

    override fun getShortName(): String {
        return "ThreadingInspector"
    }

    override fun getGroupDisplayName(): String {
        return Constants.InspectionGroupName
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.ERROR
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean): KtVisitorVoid {
        return namedFunctionVisitor { ourCallFunction: KtNamedFunction ->
            val extMan = ExternalAnnotationsManager.getInstance(ourCallFunction.project)
            val parentContext = ClassHierarchyAnnotationsCache.getClassHierarchyAnnotaions(
                    ourCallFunction.containingClassOrObject,
                    extMan).computeAnnotationContext() //infer parent if possible
            //if "ourCallFunction" does not dictate a threading , we may infer it from the parent class / object which is what happens next.
            val thisThreading = ourCallFunction.computeThreading(extMan)
                    ?: parentContext
            val isThisBackground = thisThreading?.isBackgroundThreaded ?: false
            //skip if no available data.
            if (thisThreading == null || !isThisBackground && !thisThreading.isUiThreaded) {
                //no knowledge of this method.. just skip.
                return@namedFunctionVisitor
            }
            //sanity check no one does both types of annotations at ones
            if (thisThreading.isInvalid()) {
                holder.registerProblem(ourCallFunction.nameIdentifier
                        ?: ourCallFunction, "This method is annotated both as any threaded and ui threaded. Annotate ourCallFunction according to what type its supposed to be.")
                return@namedFunctionVisitor
            }
            //now we are either of them, then collect and see if any children are the opposite type.
            //TODO consider "fast" slow threading concepts...(and potential slow) (say we can tell if a function is "maybe" slow)
            ourCallFunction.forEachDescendantOfType { exp: KtCallExpression ->
                val psiResolved = exp.resolvePsi() ?: return@forEachDescendantOfType
                //do not inspect inbuilt constructs.
                if (exp.isInbuiltConstruct()) {
                    return@forEachDescendantOfType
                }

                val annotations = psiResolved.resolveAllMethodAnnotations(extMan)
                val methodThreading = annotations.computeThreading()

                val threadTypes: Threading? = methodThreading
                        ?: ClassHierarchyAnnotationsCache.getClassHierarchyAnnotaions(
                                (psiResolved as? PsiMethod)?.containingClass,
                                extMan).computeAnnotationContext()


                if (threadTypes != null) { //we are only afraid of UI threading.
                    var haveReportedIssue = false
                    exp.goUpUntil(ourCallFunction) {
                        val x = it.computeThreadingFromTo() ?: return@goUpUntil
                        //DO not report constructs..
                        if (threadTypes.isInValidFor(x)) {
                            //threadType is the root problem (the call) the context change is where the things" went wrong".
                            holder.registerProblem(exp, threadTypes.computeErrorMessageToThis())
                            haveReportedIssue = true
                        } else {
                            return@forEachDescendantOfType //skip this, as we are in a "valid" context.
                        }
                    }
                    //if we did not exit then we have "NO" context changes , thus we are to look at the caller function
                    //DO not report constructs..
                    if (thisThreading.isInValidFor(threadTypes) && !haveReportedIssue) {
                        holder.registerProblem(exp, threadTypes.computeErrorMessageToThis())
                    }
                }
            }
        }
    }
}

fun Threading.computeErrorMessageToThis(): String {
    return if (isUiThreaded) {
        "Tries to run UI code from the background"
    } else {
        "Tries to run Background code from UI code"
    }
}

fun List<UAnnotation>.computeAnnotationContext(): Threading? {
    forEach {
        if (it.qualifiedName in uiContextNames) {
            return Threading.uiThreaded
        }
        if (it.qualifiedName in backgroundContextNames) {
            return Threading.backgroundThreaded
        }
    }
    return null
}

/**
 * Computes if this is a "thread" changing construct.
 * // android is like "runOnUiThread"
 *      functions like "post" on a view (post, postDelayed, postOnAnimation, postOnAnimationDelayed)
 *        states that the user should be on the UI thread to begin with but eg
 *        https://developer.android.com/guide/components/processes-and-threads#WorkerThreads
 *        states otherwise.
 *    there is also the AsyncTask. (https://developer.android.com/reference/android/os/AsyncTask.html#the-4-steps)
 *      - onPreExecute(), invoked on the UI
 *      - doInBackground(Params...) invoked on the background
 *      - onProgressUpdate(Progress...), (called via publishProgress(AnyThread)) invokes on the ui.
 *      - onPostExecute(Result), invoked on the UI thread
 *
 *    //the handler part is quite complex, as that is "Messaging".. but again that is "AnyThread".
 *
 *
 *  also requries to look at other types of annotations....
 *    //todo requires a test of this.
 *
 *
 * // coroutines is
 *  - launch(Dispatchers.Main){
 *  - async(Dispatchers.Main)
 * // javafx is
 *      - Platform.runLater
 *      - Task
 *      - Service
 *      --see https://www.developer.com/java/data/multithreading-in-javafx.html
 * // swing is
 *          - SwingUtilities.invokeLater
 * // java:
 *  - "Thread" (constructor, and classes inheriting and annoynumus classes) [changes to a background thread]
 *
 *  //swt
 *      - Display.getDefault().asyncExec
 *      - Display.getDefault().syncExec(
 *      //see https://www.javalobby.org//java/forums/t43753.html
 *
 */

private val uiContextNames = setOf(
        "csense.kotlin.annotations.threading.InUiContext"
)
private val backgroundContextNames = setOf(
        "csense.kotlin.annotations.threading.InBackgroundContext"
)

private val toUiThreadFunctionNames = setOf(
        //android
        "android.app.Activity.runOnUiThread", //to ui
        "android.view.View.post",//to ui
        "android.view.View.postDelayed",//to ui
        "android.view.View.postOnAnimation",//to ui
        "android.view.View.postOnAnimationDelayed", //to ui
        //java fx
        "javafx.application.Platform.runLater", //to ui
        //swing
        "javax.swing.SwingUtilities.invokeLater", //to ui
        "org.eclipse.swt.widgets.Display.asyncExec", //to ui
        "org.eclipse.swt.widgets.Display.syncExec", //to ui
        //csense
        "csense.kotlin.extensions.coroutines.launchMain",
        "csense.kotlin.extensions.coroutines.asyncMain",
        "csense.kotlin.extensions.coroutines.withContextMain"
)

private val contextChangeToBackgroundNames = setOf(
        "java.lang.Thread.Thread",
        "csense.kotlin.extensions.coroutines.launchDefault",
        "csense.kotlin.extensions.coroutines.asyncDefault",
        "csense.kotlin.extensions.coroutines.withContextDefault"
)

private data class ContextChangeNameToType(
        val mainThreadNames: Set<String>,
        val otherTypeNames: Set<String>)

private val kotlinCoroutineContextNames = ContextChangeNameToType(
        setOf("Main", "Dispatchers.Main"),
        setOf(
                "IO", "Dispatchers.IO",
                "Default", "Dispatchers.Default",
                "Unconfined", "Dispatchers.Unconfined"))

private val contextChangeParameter = setOf(
        "kotlinx.coroutines.launch",
        "kotlinx.coroutines.async",
        "kotlinx.coroutines.withContext"
)

//if we go from x to a background thread / worker thread
fun PsiElement.isInbuiltThreadChangeToBackground(): Boolean {
    if (this is KtCallExpression) {
        val psi = this.resolvePsi() ?: return false
        return when (psi) {
            is KtLightMethod -> psi.getKotlinFqNameString() in contextChangeToBackgroundNames
            is KtNamedFunction -> psi.getKotlinFqNameString() in contextChangeToBackgroundNames
            is PsiMethod -> psi.getKotlinFqNameString() in contextChangeToBackgroundNames
            else -> false
        }
    }
    return false
}

fun PsiElement.isInbuiltConstruct(): Boolean {
    val name = if (this is KtCallExpression) {
        val psi = this.resolvePsi() ?: return false
        psi.getKotlinFqNameString()
    } else {
        getKotlinFqNameString()
    }
    return name in toUiThreadFunctionNames ||
            name in contextChangeToBackgroundNames ||
            name in contextChangeParameter
}

fun PsiElement.isInbuiltThreadChangeToUI(): Boolean {
    if (this is KtCallExpression) {
        val psi = this.resolvePsi() ?: return false
        return when (psi) {
            is KtLightMethod -> psi.getKotlinFqNameString() in toUiThreadFunctionNames
            is KtNamedFunction -> psi.getKotlinFqNameString() in toUiThreadFunctionNames
            is PsiMethod -> psi.getKotlinFqNameString() in toUiThreadFunctionNames
            else -> false
        }
    }
    return false
}

/**
 * Computes whenever we are in a thread change, and if what is the resulting threading.
 * @receiver PsiElement
 * @return Threading? null if not a thread changing construct, or the resulting threading.
 */
fun PsiElement.computeThreadingFromTo(): Threading? {
    if (this.isInbuiltThreadChangeToUI()) {
        return Threading.uiThreaded
    }
    if (this.isInbuiltThreadChangeToBackground()) {
        return Threading.backgroundThreaded
    }
    return this.computeInBuiltThreadingChange()
}

fun PsiElement.computeInBuiltThreadingChange(): Threading? {
    if (this is KtCallExpression) {
        val psi = this.resolvePsi() ?: return null
        val isCorountine = when (psi) {
            is KtLightMethod -> psi.getKotlinFqNameString() in contextChangeParameter
            is KtNamedFunction -> psi.getKotlinFqNameString() in contextChangeParameter
            is PsiMethod -> psi.getKotlinFqNameString() in contextChangeParameter
            else -> false
        }
        if (!isCorountine) {

            return null
        }
        //analyze first param
        val firstName = valueArguments.firstOrNull()?.text ?: return null
        //we have no clue ??
        return when (firstName) {
            in kotlinCoroutineContextNames.mainThreadNames ->
                Threading.uiThreaded
            in kotlinCoroutineContextNames.otherTypeNames ->
                Threading.backgroundThreaded
            else -> null
        }
    }
    return null
}

inline fun PsiElement.goUpUntil(parent: PsiElement, action: Function0<PsiElement>) {
    var current: PsiElement? = this.parent
    while (current != null && current != parent) {
        action(current)
        current = current.parent
    }
}

fun List<UAnnotation>.computeThreading(): Threading? {
    val background = computeIsAnyBackgroundThread()
    val ui = computeIsUiThread()
    if (!ui && !background) {
        return null
    }
    return Threading(background, ui)
}

fun List<UAnnotation>.computeIsAnyBackgroundThread(): Boolean = any {
    it.qualifiedName in backgroundThreadNames
}

fun List<UAnnotation>.computeIsUiThread(): Boolean = any {
    it.qualifiedName in uiAnnotationsNames
}

fun KtCallExpression.resolvePsi(): PsiElement? {
    return (this.toUElement() as? UResolvable)?.resolve()
            ?: getCalleeExpressionIfAny()?.resolveMainReferenceToDescriptors()?.firstOrNull()?.findPsi() ?: return null
}


fun KtFunction.computeThreading(extLookup: ExternalAnnotationsManager): Threading? {
    val annotations = resolveAllMethodAnnotations(extLookup)
    return annotations.computeThreading()
}

data class Threading(val isBackgroundThreaded: Boolean, val isUiThreaded: Boolean) {
    companion object {
        val uiThreaded = Threading(
                isBackgroundThreaded = false,
                isUiThreaded = true)
        val backgroundThreaded = Threading(
                isBackgroundThreaded = true,
                isUiThreaded = false)
    }
}

/**
 * If this is an invalid threading setting.
 * @receiver Threading
 * @return Boolean
 */
fun Threading.isInvalid(): Boolean = this.isUiThreaded && this.isBackgroundThreaded

fun Threading.isInValidFor(other: Threading): Boolean = !isValidFor(other)
fun Threading.isValidFor(other: Threading): Boolean {
    return when {
        //if any is invalid the whole result is invalid.
        this.isInvalid() || other.isInvalid() -> false
        //any thing mixed with any thread is "ok"
        this.isAnyThread || other.isAnyThread -> true
        //same type => ok
        this.isUiThreaded && !this.isBackgroundThreaded -> {
            other.isUiThreaded && !other.isBackgroundThreaded
        }
        //same type => ok
        this.isBackgroundThreaded && !this.isUiThreaded -> {
            other.isBackgroundThreaded && !other.isUiThreaded
        }
        //any kind of mixing.
        else -> false
    }
}

/**
 * If not annotated with ui or background it can just be "any" thread.
 */
val Threading.isAnyThread: Boolean
    get() = !this.isBackgroundThreaded && !this.isUiThreaded


//old annotations for android - https://developer.android.com/reference/android/support/annotation/package-summary.html
val backgroundThreadNames = setOf(
        "csense.kotlin.annotations.threading.BackgroundThread",//csense //TODO remove
        "csense.kotlin.annotations.threading.InBackground",//csense
        "android.support.annotation.WorkerThread",
        "androidx.annotation.WorkerThread" //https://androidx.tech/artifacts/annotation/annotation/1.0.2-source/androidx/annotation/WorkerThread.java.html
)

val uiAnnotationsNames = setOf(
        "csense.kotlin.annotations.threading.InUi",  //csense
        "androidx.annotation.UiThread", //https://androidx.tech/artifacts/annotation/annotation/1.0.2-source/androidx/annotation/UiThread.java.html
        "androidx.annotation.MainThread", //https://androidx.tech/artifacts/annotation/annotation/1.0.2-source/androidx/annotation/MainThread.java.html
        "android.support.annotation.UiThread",
        "android.support.annotation.MainThread"
)
