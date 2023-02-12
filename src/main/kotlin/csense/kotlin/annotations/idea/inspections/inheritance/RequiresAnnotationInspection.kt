package csense.kotlin.annotations.idea.inspections.inheritance

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.kotlin.annotations.idea.*
import csense.kotlin.annotations.idea.analyzers.*
//import csense.kotlin.annotations.idea.analyzers.requiresAnnotation.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*

class RequiresAnnotationInspection : AbstractKotlinInspection() {
    override fun getDisplayName(): String {
        return "RequiresAnnotationInspection"
    }

    override fun getStaticDescription(): String {
        return """
            
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String {
        return "more desc ? "
    }

    override fun getShortName(): String {
        return "AnnotationRequired"
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
    ): KtVisitorVoid = classOrObjectVisitor { clzOrObject ->
//        val result = RequiresAnnotationAnalyzer.analyze(clzOrObject)
//        result.errors.forEach { error: AnalyzerError -> holder.registerProblem(error) }
    }
}