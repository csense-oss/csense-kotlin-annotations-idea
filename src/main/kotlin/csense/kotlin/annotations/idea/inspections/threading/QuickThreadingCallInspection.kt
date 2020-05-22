package csense.kotlin.annotations.idea.inspections.threading

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import csense.idea.base.UastKtPsi.*
import csense.idea.base.bll.psi.goUpUntil
import csense.idea.base.cache.ClassHierarchyAnnotationsCache
import csense.idea.base.mpp.MppAnnotation
import csense.idea.base.mpp.resolveAllMethodAnnotationMppAnnotation
import csense.idea.base.mpp.toMppAnnotations
import csense.kotlin.annotations.idea.Constants
import csense.kotlin.annotations.idea.analyzers.*
import csense.kotlin.annotations.idea.analyzers.threading.*
import csense.kotlin.extensions.*
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
            This inspection provides highlighting for mixing of ui / background designed work.
            In most ui frameworks you are only allowed to update the ui from a specific thread, thus this can help with that.
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
            val result = logMeasureTimeInMillis {
                ThreadingCallInspectionAnalyzer.analyze(ourCallFunction)
            }
            result.errors.forEach { holder.registerProblem(it) }
        }
    }
}
