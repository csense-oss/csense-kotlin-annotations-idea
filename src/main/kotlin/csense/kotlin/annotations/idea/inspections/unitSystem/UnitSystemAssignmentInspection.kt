package csense.kotlin.annotations.idea.inspections.unitSystem

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.kotlin.annotations.idea.*
import csense.kotlin.annotations.idea.analyzers.*
//import csense.kotlin.annotations.idea.analyzers.noEscape.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*

class UnitSystemAssignmentInspection: AbstractKotlinInspection() {
    override fun getDisplayName(): String {
        return "UnitSystemAssignmentInspection"
    }
    
    override fun getStaticDescription(): String {
        return """
        
        """.trimIndent()
    }
    
    override fun getDescriptionFileName(): String {
        return "more desc ? "
    }
    
    override fun getShortName(): String {
        return "UnitSystemAssignmentInspection"
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
    
    /**
     *
     */
    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean
    ): KtVisitorVoid {
        return expressionVisitor {
        
        }
    }
}