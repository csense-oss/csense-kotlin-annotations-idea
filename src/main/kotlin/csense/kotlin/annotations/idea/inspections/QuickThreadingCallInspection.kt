package csense.kotlin.annotations.idea.inspections

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import csense.idea.base.UastKtPsi.getKotlinFqNameString
import csense.idea.base.bll.psi.goUpUntil
import csense.idea.base.cache.ClassHierarchyAnnotationsCache
import csense.idea.base.mpp.MppAnnotation
import csense.idea.base.mpp.resolveAllMethodAnnotationMppAnnotation
import csense.idea.base.mpp.toMppAnnotations
import csense.kotlin.annotations.idea.Constants
import csense.kotlin.extensions.map
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


class QuickThreadingCallInspection : AbstractKotlinInspection() {
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
            val parentContext = ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(
                    ourCallFunction.containingClassOrObject,
                    extMan).computeAnnotationContext() //infer parent if possible
            //if "ourCallFunction" does not dictate a threading , we may infer it from the parent class / object which is what happens next.
            val thisThreading = ourCallFunction.computeThreading(extMan)
                    ?: parentContext
                    ?: return@namedFunctionVisitor//no knowledge of this method.. just skip.

            //skip if no available data.
            //sanity check no one does both types of annotations at ones

            if (ourCallFunction.computeIfThreadingIsInvalid(extMan)) {
                holder.registerProblem(ourCallFunction.nameIdentifier
                        ?: ourCallFunction, "This method is annotated both as any threaded and ui threaded. Annotate ourCallFunction according to what type its supposed to be.")
                return@namedFunctionVisitor
            }
            //now we are either of them, then collect and see if any children are the opposite type.
            ourCallFunction.forEachDescendantOfType { exp: KtCallExpression ->
                val psiResolved = exp.resolvePsi() ?: return@forEachDescendantOfType
                //do not inspect inbuilt constructs.
                if (exp.isInbuiltConstruct()) {
                    return@forEachDescendantOfType
                }

                val methodThreading = psiResolved.computeThreading(extMan)

                val threadTypes: Threading? = methodThreading ?: when (psiResolved) {
                    is PsiMethod -> ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(
                            psiResolved.containingClass,
                            extMan).computeAnnotationContext()
                    is KtFunction -> ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(
                            psiResolved.containingClassOrObject,
                            extMan).computeAnnotationContext()
                    else -> null
                }

                if (threadTypes != null) { //we are only afraid of UI threading.
                    var haveReportedIssue = false
                    exp.goUpUntil(ourCallFunction) {
                        val x = it.computeThreadingFromTo() ?: return@goUpUntil
                        //DO not report constructs..
                        if (threadTypes.isInValidFor(x)) {
                            //threadType is the root problem (the call) the context change is where the things" went wrong".
                            holder.registerProblem(exp, threadTypes.computeErrorMessageTo(x))
                            haveReportedIssue = true
                        } else {
                            return@forEachDescendantOfType //skip this, as we are in a "valid" context.
                        }
                    }
                    //if we did not exit then we have "NO" context changes , thus we are to look at the caller function
                    //DO not report constructs..
                    if (thisThreading.isInValidFor(threadTypes) && !haveReportedIssue) {
                        holder.registerProblem(exp, thisThreading.computeErrorMessageTo(threadTypes))
                    }
                }
            }

            ourCallFunction.forEachDescendantOfType<KtNameReferenceExpression> { exp ->
                val psiResolved = exp.resolvePsi() ?: return@forEachDescendantOfType
                //do not inspect inbuilt constructs.
                if (exp.isInbuiltConstruct() || psiResolved !is KtProperty) {
                    return@forEachDescendantOfType
                }

                val methodThreading = psiResolved.computeThreading(extMan)

                val threadTypes: Threading? = methodThreading ?: when (psiResolved) {
                    is PsiMethod -> ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(
                            psiResolved.containingClass,
                            extMan).computeAnnotationContext()
                    is KtFunction -> ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(
                            psiResolved.containingClassOrObject,
                            extMan).computeAnnotationContext()
                    else -> null
                }
                if (threadTypes != null) { //we are only afraid of UI threading.
                    var haveReportedIssue = false
                    exp.goUpUntil(ourCallFunction) {
                        val x = it.computeThreadingFromTo() ?: return@goUpUntil
                        //DO not report constructs..
                        if (threadTypes.isInValidFor(x)) {
                            //threadType is the root problem (the call) the context change is where the things" went wrong".
                            holder.registerProblem(exp, threadTypes.computeErrorMessageTo(x))
                            haveReportedIssue = true
                        } else {
                            return@forEachDescendantOfType //skip this, as we are in a "valid" context.
                        }
                    }
                    //if we did not exit then we have "NO" context changes , thus we are to look at the caller function
                    //DO not report constructs..
                    if (thisThreading.isInValidFor(threadTypes) && !haveReportedIssue) {
                        holder.registerProblem(exp, thisThreading.computeErrorMessageTo(threadTypes))
                    }
                }
            }

        }
    }
}

fun Threading.computeErrorMessageToThis(): String {
    return if (this == Threading.UIThreaded) {
        "Tries to run UI code from the background"
    } else {
        "Tries to run Background code from UI code"
    }
}

fun Threading.computeErrorMessageTo(other: Threading): String {
    return "Trying to call ${other.computeName()} from ${this.computeName()}"
}


fun Threading.computeName(): String {
    return when (this) {
        Threading.UIThreaded -> "UI thread"
        Threading.AnyThreaded -> "Any thread"
        Threading.BackgroundThreaded -> "Background / worker thread"
    }
}

fun List<MppAnnotation>.computeAnnotationContext(): Threading? {
    forEach {
        if (it.qualifiedName in uiContextNames) {
            return Threading.UIThreaded
        }
        if (it.qualifiedName in backgroundContextNames) {
            return Threading.BackgroundThreaded
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
        return Threading.UIThreaded
    }
    if (this.isInbuiltThreadChangeToBackground()) {
        return Threading.BackgroundThreaded
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
                Threading.UIThreaded
            in kotlinCoroutineContextNames.otherTypeNames ->
                Threading.BackgroundThreaded
            else -> null
        }
    }
    return null
}

fun List<MppAnnotation>.computeThreading(): Threading? = when {
    computeIsBackgroundThread() -> Threading.BackgroundThreaded
    computeIsUiThread() -> Threading.UIThreaded
    computeIsAnyThreaded() -> Threading.AnyThreaded
    else -> null
}

fun List<MppAnnotation>.computeIsAnyThreaded(): Boolean = any {
    it.qualifiedName in anyThreadedNames
}

fun List<MppAnnotation>.computeIsBackgroundThread(): Boolean = any {
    it.qualifiedName in backgroundThreadNames
}

fun List<MppAnnotation>.computeIsUiThread(): Boolean = any {
    it.qualifiedName in uiAnnotationsNames
}

fun PsiElement.computeThreading(extLookup: ExternalAnnotationsManager): Threading? {
    val annotations = resolveAllMethodAnnotationMppAnnotation(extLookup)
    return annotations.computeThreading()
}

fun KtProperty.computeThreading(extLookup: ExternalAnnotationsManager): Threading? {
    //TODO external annotations ?!?
    val annotations = annotationEntries.toMppAnnotations()
    return annotations.computeThreading()
}

enum class Threading {
    UIThreaded,
    AnyThreaded,
    BackgroundThreaded
}

fun PsiElement.computeIfThreadingIsInvalid(extLookup: ExternalAnnotationsManager): Boolean {
    val annotations = resolveAllMethodAnnotationMppAnnotation(extLookup)
    return annotations.isInvalidThreadingAnnotations()
}

fun List<MppAnnotation>.isInvalidThreadingAnnotations(): Boolean {
    var foundTypes = 0
    foundTypes += computeIsUiThread().map(1, 0)
    foundTypes += computeIsAnyThreaded().map(1, 0)
    foundTypes += computeIsBackgroundThread().map(1, 0)
    return foundTypes > 1
}

fun Threading.isInValidFor(other: Threading): Boolean = !isValidFor(other)

fun Threading.isValidFor(other: Threading): Boolean = when {
    //any thing mixed with any thread is "ok" (any calling  out however is NOT ok).
    other == Threading.AnyThreaded -> true
    //same type => ok
    this == other -> true
    //same type => ok
    //any kind of mixing.
    else -> false
}


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

val anyThreadedNames = setOf(
        "csense.kotlin.annotations.threading.InAny",
        "androidx.annotation.AnyThread", //https://androidx.tech/artifacts/annotation/annotation/1.0.2-source/androidx/annotation/AnyThread.java.html
        "android.support.annotation.AnyThread"
)


//TODO remove for base lib.(0.0.8)
fun KtReferenceExpression.resolvePsi(): PsiElement? {
    return (this.toUElement() as? UResolvable)?.resolve()
            ?: getCalleeExpressionIfAny()?.resolveMainReferenceToDescriptors()?.firstOrNull()?.findPsi() ?: return null
}