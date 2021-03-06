package csense.kotlin.annotations.idea.inspections.sideeffect

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.kotlin.annotations.idea.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*

class NoEscapeInspection : AbstractKotlinInspection() {
    override fun getDisplayName(): String {
        return "NoEscapeInspection"
    }
    
    override fun getStaticDescription(): String? {
        return """
            
        """.trimIndent()
    }
    
    override fun getDescriptionFileName(): String? {
        return "more desc ? "
    }
    
    override fun getShortName(): String {
        return "NoEscapeInspection"
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
    
    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean
    ): KtVisitorVoid {
        //Types of escape:
        // - parsing as argument to function where its not marked NoEscape (as that means its allowed to escape) (semi difficult)
        // - for .let, apply ect we should inspect the lambda.. which can get quite tricky. (hard)
        // -
        return parameterVisitor {
            //TODO make me
        }
    }
    
}