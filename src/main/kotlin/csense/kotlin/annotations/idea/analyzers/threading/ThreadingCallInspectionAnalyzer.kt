package csense.kotlin.annotations.idea.analyzers.threading

import com.intellij.codeInsight.*
import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.*
import csense.idea.base.bll.psi.getKotlinFqNameString
import csense.idea.base.cache.*
import csense.idea.base.mpp.*
import csense.idea.base.uastKtPsi.*
import csense.kotlin.annotations.idea.analyzers.*
import csense.kotlin.extensions.*
import csense.kotlin.extensions.collections.*
import org.jetbrains.kotlin.asJava.elements.*
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.lexer.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

object ThreadingCallInspectionAnalyzer {

    fun analyze(ourCallFunction: KtNamedFunction): AnalyzerResult {
        val errors = mutableListOf<AnalyzerError>()
        
        val extMan = ExternalAnnotationsManager.getInstance(ourCallFunction.project)
        val parentContext = ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(
                ourCallFunction.containingClassOrObject,
                extMan).computeThreadingContext() //infer parent if possible
        //if "ourCallFunction" does not dictate a threading , we may infer it from the parent class / object which is what happens next.
        val thisThreading = ourCallFunction.computeThreading(extMan)
                ?: parentContext
                ?: return AnalyzerResult(errors)//no knowledge of this method.. just skip. //TODO look for things that changes the context to known ones
        
        //skip if no available data.
        //sanity check no one does both types of annotations at ones
        
        if (ourCallFunction.computeIfThreadingIsInvalid(extMan)) {
            errors.add(AnalyzerError(
                    ourCallFunction.nameIdentifier ?: ourCallFunction,
                    "This method is annotated with more than one threading type, please choose what this is...",
                    arrayOf()))
            return AnalyzerResult(errors)
        }
        //now we are either of them, then collect and see if any function invocation are of the opposite type.
        ourCallFunction.forEachDescendantOfType { exp: KtCallExpression ->
            //do not inspect inbuilt constructs.
            if (exp.isInbuiltConstruct()) {
                return@forEachDescendantOfType
            }
            val psiResolved = exp.resolvePsi() ?: return@forEachDescendantOfType
            
            
            val methodThreading = psiResolved.computeThreading(extMan)
            
            val threadTypes: Threading? = methodThreading ?: when (psiResolved) {
                is PsiMethod -> ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(
                        psiResolved.containingClass,
                        extMan).computeThreadingContext()
                is KtFunction -> ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(
                        psiResolved.containingClassOrObject,
                        extMan).computeThreadingContext()
                else -> null
            }
            
            if (threadTypes != null) {
                var haveReportedIssue = false
                exp.goUpUntil(ourCallFunction) {
                    //if we are in a lambda we are to verify that it itself is not marked / annotated.
                    val x = it.computeThreadingFromTo() ?: return@goUpUntil
                    //DO not report constructs..
                    if (x.isInValidFor(threadTypes)) {
                        //threadType is the root problem (the call) the context change is where the things" went wrong".
                        errors.add(AnalyzerError(
                                exp,
                                x.computeErrorMessageTo(threadTypes),
                                arrayOf()))
                        haveReportedIssue = true
                    } else {
                        return@forEachDescendantOfType //skip this, as we are in a "valid" context.
                    }
                }
                //if we did not exit then we have "NO" context changes , thus we are to look at the caller function
                //DO not report constructs..
                if (thisThreading.isInValidFor(threadTypes) && !haveReportedIssue) {
                    errors.add(AnalyzerError(
                            exp,
                            thisThreading.computeErrorMessageTo(threadTypes),
                            arrayOf()))
                }
            }
        }
        //look for variable access
        ourCallFunction.forEachDescendantOfType<KtNameReferenceExpression> { exp ->
            val psiResolved = exp.findPsi() ?: return@forEachDescendantOfType
            //do not inspect inbuilt constructs.
            if (exp.isInbuiltConstruct() || psiResolved !is KtProperty || psiResolved.isLocal) { //if it is a local variable just skip it for now, as later it should be computed correctly.
                return@forEachDescendantOfType
            }
            
            val methodThreading = psiResolved.computeThreading(extMan, exp)
            
            val accessingThreadType: Threading? = methodThreading ?: when (psiResolved) {
                is PsiMethod -> ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(
                        psiResolved.containingClass,
                        extMan).computeThreadingContext()
                is KtFunction -> ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(
                        psiResolved.containingClassOrObject,
                        extMan).computeThreadingContext()
                else -> null
            }
            if (accessingThreadType != null) { //we are only afraid of UI threading.
                var haveReportedIssue = false
                exp.goUpUntil(ourCallFunction) {
                    val expCurrentContext = it.computeThreadingFromTo() ?: return@goUpUntil
                    //DO not report constructs..
                    if (expCurrentContext.isInValidFor(accessingThreadType)) {
                        //threadType is the root problem (the call) the context change is where the things" went wrong".
                        errors.add(AnalyzerError(
                                exp,
                                accessingThreadType.computeErrorMessageTo(expCurrentContext),
                                arrayOf()))
                        haveReportedIssue = true
                    } else {
                        return@forEachDescendantOfType //skip this, as we are in a "valid" context.
                    }
                }
                //if we did not exit then we have "NO" context changes , thus we are to look at the caller function
                //DO not report constructs..
                if (thisThreading.isInValidFor(accessingThreadType) && !haveReportedIssue) {
                    errors.add(AnalyzerError(
                            exp,
                            thisThreading.computeErrorMessageTo(accessingThreadType),
                            arrayOf()))
                }
            }
        }
        return AnalyzerResult(errors)
    }
}


fun Threading.computeErrorMessageTo(other: Threading): String {
    return "Trying to access a `${other.computeName()}` from a `${this.computeName()}`"
}


fun Threading.computeName(): String {
    return when (this) {
        Threading.UIThreaded -> "UI thread"
        Threading.AnyThreaded -> "Any thread"
        Threading.BackgroundThreaded -> "Background / worker thread"
    }
}

fun List<MppAnnotation>.computeThreadingContext(): Threading? {
    forEach {
        if (it.qualifiedName in uiContextNames) {
            return@computeThreadingContext Threading.UIThreaded
        }
        if (it.qualifiedName in backgroundContextNames) {
            return@computeThreadingContext Threading.BackgroundThreaded
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
        val otherTypeNames: Set<String>
)

private val kotlinCoroutineContextNames = ContextChangeNameToType(
        setOf("Main", "Dispatchers.Main"),
        setOf(
                "IO", "Dispatchers.IO",
                "Default", "Dispatchers.Default",
                "Unconfined", "Dispatchers.Unconfined"))

private val contextChangeParameter = setOf(
        "kotlinx.coroutines.launch",
        "kotlinx.coroutines.async",
        "kotlinx.coroutines.withContext",
        "kotlinx.coroutines.runBlocking"
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
    
    if (this is KtLambdaArgument) {
        val call = this.parent as? KtCallExpression
        val parmIndex = call?.valueArguments?.indexOfOrNull(this)
        val fnc = call?.resolveMainReference() as? KtNamedFunction
        if (parmIndex != null) {
            val lambdaParam = fnc?.valueParameters?.getOrNull(parmIndex)
            val annotations = lambdaParam?.annotationEntries?.toMppAnnotations()
            val annotationsThreading = annotations?.computeThreading()
            if (annotationsThreading != null) {
                return annotationsThreading
            }
        }
    }
    
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

fun KtProperty.computeThreading(extMan: ExternalAnnotationsManager, exp: KtExpression): Threading? {
    val expPar = exp.parent
    val expParLeft = (expPar as? KtBinaryExpression)?.left
    val isSetter =
            expPar is KtBinaryExpression &&
                    expPar.operationToken == KtTokens.EQ &&
                    expParLeft is KtNameReferenceExpression &&
                    expParLeft.findPsi() == this
    
    val annotations = annotationEntries.toMppAnnotations2() +
            (isSetter.map(setter, getter)?.annotationEntries?.toMppAnnotations2() ?: listOf())
//
    val result = annotations.computeThreading()
    return result ?: containingClass()
            ?.resolveAllClassMppAnnotation(extMan)
            ?.computeThreadingContext()
}
fun List<KtAnnotationEntry>.toMppAnnotations2(): List<MppAnnotation> = mapNotNull { it.toMppAnnotation2() }
fun KtAnnotationEntry.toMppAnnotation2(): MppAnnotation? {

    //first light
    val qualifiedName = this.toLightAnnotation()?.qualifiedName
    if (qualifiedName != null) {
        return MppAnnotation(qualifiedName)
    }
    //then the "direct" kotlin way.
    val ktName = this.typeReference?.resolve()?.findParentOfType<KtClass>()?.getKotlinFqNameString()
    if (ktName != null) {
        return MppAnnotation(ktName)
    }
    //failed.
    return null
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

//This is "calling to" and "other" is the context we are given.
fun Threading.isValidFor(
        other: Threading
): Boolean {
    //any thing mixed with any thread is "ok" (any calling  out however is NOT ok).
    if (other == Threading.AnyThreaded) {
        return true
    }
    return this == other
}


//old annotations for android - https://developer.android.com/reference/android/support/annotation/package-summary.html
val backgroundThreadNames = setOf(
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
