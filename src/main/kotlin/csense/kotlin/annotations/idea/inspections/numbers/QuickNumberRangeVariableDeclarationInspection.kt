package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.kotlin.annotations.idea.*
import csense.kotlin.annotations.idea.inspections.numbers.bll.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*


class QuickNumberRangeVariableDeclarationInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "NumberVariableRangeInspector"
    }

    override fun getStaticDescription(): String {
        return """
            Validates that the initialization of a number variable (with limits) are obeyed.
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String {
        return "Validates that the initialization of a number variable (with limits) are obeyed."
    }

    override fun getShortName(): String {
        return "NumberVariableRangeInspection"
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
    ): KtVisitorVoid = propertyVisitor { property: KtProperty ->
        property.validateNumberRangeForInitializer(holder)
    }
}
