package csense.kotlin.annotations.idea.inspections.inheritance

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.codeInspection.ProblemsHolder
import csense.idea.base.cache.ClassHierarchyAnnotationsCache
import csense.kotlin.annotations.idea.Constants
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.psi.classOrObjectVisitor

class ParameterLessConstructorRequriedInspection : AbstractKotlinInspection() {
    override fun getDisplayName(): String {
        return "ParameterLessConstructorInspection"
    }

    override fun getStaticDescription(): String? {
        return """
            
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String? {
        return "more desc ? "
    }

    override fun getShortName(): String {
        return "ParameterLessConstructorInspection"
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
    ): KtVisitorVoid = classOrObjectVisitor { classOrObject ->
        val project = classOrObject.project
        val extMgr = ExternalAnnotationsManager.getInstance(project)
        val annotations = ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(classOrObject, extMgr)
        if (!annotations.any { it.qualifiedName in parameterLessConstructorNames }) {
            return@classOrObjectVisitor //do not do anymore work
        }
        val allConstructors = classOrObject.secondaryConstructors + (classOrObject.primaryConstructor?.let { listOf(it) }
                ?: emptyList())
        if (!allConstructors.any { it.getValueParameters().isEmpty() }) {
            holder.registerProblem(
                    classOrObject.nameIdentifier ?: classOrObject,
                    "You need a parameter less constructor (as per the ParameterLessConstructor interface)")
        }
    }

    val parameterLessConstructorNames = setOf(
            "csense.kotlin.annotations.inheritance.ParameterLessConstructorRequired"
    )
}