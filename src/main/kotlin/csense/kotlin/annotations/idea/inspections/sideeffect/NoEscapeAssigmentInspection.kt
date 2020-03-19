package csense.kotlin.annotations.idea.inspections.sideeffect

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.codeInspection.ProblemsHolder
import csense.idea.base.annotationss.resolveAnnotationsKt
import csense.idea.base.bll.psi.findParentOfType
import csense.kotlin.annotations.idea.Constants
import csense.kotlin.annotations.idea.analyzers.*
import csense.kotlin.annotations.idea.analyzers.noEscape.*
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.*
import org.jetbrains.uast.UAnnotation

class NoEscapeAssigmentInspection : AbstractKotlinInspection() {
    override fun getDisplayName(): String {
        return "NoEscapeAssignmentInspection"
    }
    
    override fun getStaticDescription(): String? {
        return """
            When a given parameter is marked as "NoEscape" , then this inspection prevents / errors out when you try to assign it to a variable.
        """.trimIndent()
    }
    
    override fun getDescriptionFileName(): String? {
        return "more desc ? "
    }
    
    override fun getShortName(): String {
        return "NoEscapeAssignmentInspection"
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

        return expressionVisitor { exp: KtExpression ->
            val result = NoEscapeAssignmentAnalyzer.analyze(exp)
            result.errors.forEach { error: AnalyzerError -> holder.registerProblem(error) }
        }
        
    }
}
