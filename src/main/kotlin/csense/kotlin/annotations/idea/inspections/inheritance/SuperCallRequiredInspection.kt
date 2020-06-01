package csense.kotlin.annotations.idea.inspections.inheritance

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.kotlin.findOverridingImpl
import csense.idea.base.mpp.resolveAllMethodAnnotationMppAnnotation
import csense.kotlin.annotations.idea.Constants
import csense.kotlin.annotations.idea.quickfixes.AddSuperCallQuickFix
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

class SuperCallRequiredInspection : AbstractKotlinInspection() {
    override fun getDisplayName(): String {
        return "SuperCallRequiredInspection"
    }

    override fun getStaticDescription(): String? {
        return """
            
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String? {
        return "more desc ? "
    }

    override fun getShortName(): String {
        return "SuperCallRequiredInspection"
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
                              isOnTheFly: Boolean
    ): KtVisitorVoid = namedFunctionVisitor { fnc ->
        if (fnc.isNotOverriding()){
            return@namedFunctionVisitor
        }
        val overridingFnc: KtNamedFunction = fnc.findOverridingImpl() ?: return@namedFunctionVisitor
        val doesCallSuper = fnc.doesCallSuperFunction()
        if (doesCallSuper) {
            return@namedFunctionVisitor
        }

        val project = fnc.project
        val extMgr = ExternalAnnotationsManager.getInstance(project)

        val annotations = overridingFnc.resolveAllMethodAnnotationMppAnnotation(extMgr)
        if (!annotations.any { it.qualifiedName in superCallRequiredAnnotationNames }) {
            return@namedFunctionVisitor //do not do anymore work
        }
        holder.registerProblem(
                fnc.nameIdentifier ?: fnc,
                "You do not call super on an overridden method annotated to require a super call",
                AddSuperCallQuickFix(fnc))
    }

    val superCallRequiredAnnotationNames = setOf(
            "csense.kotlin.annotations.inheritance.SuperCallRequired",
            "androidx.annotation.CallSuper",
            "android.support.annotation.CallSuper"
    )
}
