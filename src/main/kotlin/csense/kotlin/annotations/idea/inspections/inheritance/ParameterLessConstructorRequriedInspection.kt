package csense.kotlin.annotations.idea.inspections.inheritance

import com.intellij.codeHighlighting.*
import com.intellij.codeInsight.*
import com.intellij.codeInspection.*
import csense.idea.base.bll.*
import csense.idea.base.cache.*
import csense.kotlin.annotations.idea.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*

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
            return@classOrObjectVisitor
        }
        val allConstructors = classOrObject.secondaryConstructors + (classOrObject.primaryConstructor?.let { listOf(it) }
                ?: emptyList())
        if (!allConstructors.any { it.getValueParameters().isEmpty() }) {
            holder.registerProblemSafe(
                    classOrObject.nameIdentifier ?: classOrObject,
                    "You need a parameter less constructor (as per the ParameterLessConstructor interface)")
        }
    }
    
    val parameterLessConstructorNames = setOf(
            "csense.kotlin.annotations.inheritance.ParameterLessConstructorRequired"
    )
}