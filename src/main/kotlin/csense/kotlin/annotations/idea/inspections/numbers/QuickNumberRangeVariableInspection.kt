package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.*
import com.intellij.codeInsight.*
import com.intellij.codeInspection.*
import csense.idea.base.annotations.*
import csense.idea.base.bll.*
import csense.kotlin.annotations.idea.*
import csense.kotlin.annotations.idea.bll.*
import csense.kotlin.annotations.idea.inspections.numbers.bll.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.types.typeUtil.*


class QuickNumberRangeVariableInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "NumberVariableRangeInspector"
    }

    override fun getStaticDescription(): String {
        return """
            
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String {
        return "more desc ? "
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
    ): KtVisitorVoid {
        return propertyVisitor { prop ->
            val isNumber = prop.resolveType2()?.isPrimitiveNumberOrNullableType() == true
            if (!isNumber) {
                return@propertyVisitor
            }
            val defaultExpression = prop.initializer ?: return@propertyVisitor
            val annotations = prop.resolveAnnotations(ExternalAnnotationsManager.getInstance(prop.project))
            val rangeParser = RangeParser.parse(annotations) ?: return@propertyVisitor
            val isInvalid = !rangeParser.isValid(annotations, defaultExpression)
            if (isInvalid) {
                val potentialErrorMessage = rangeParser.computeErrorMessage(annotations, defaultExpression)
                if (potentialErrorMessage != null) {
                    holder.registerProblemSafe(
                        psiElement = defaultExpression,
                        descriptionTemplate = potentialErrorMessage
                    )
                }
            }
        }
    }
}
