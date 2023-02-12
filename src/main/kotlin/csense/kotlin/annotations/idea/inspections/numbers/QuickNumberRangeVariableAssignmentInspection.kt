package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.kotlin.models.*
import csense.kotlin.annotations.idea.*
//import csense.kotlin.annotations.idea.inspections.numbers.bll.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*


class QuickNumberRangeVariableAssignmentInspection : AbstractKotlinInspection() {

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
    ): KtVisitorVoid = binaryExpressionVisitor { expression ->
//        if (expression.isNotAssignment) {
//            return@binaryExpressionVisitor
//        }
//        val lhs = expression.left ?: return@binaryExpressionVisitor
//        val rhs = expression.right ?: return@binaryExpressionVisitor
//        val reference = lhs.resolveAsReferenceToPropertyOrValueParameter() ?: return@binaryExpressionVisitor
//
//        reference.declaration.validateNumberRangeFor(
//            expression = rhs,
//            isVarArg = reference.isVarArg(),
//            typeReference = reference.typeReference(),
//            holder = holder
//        )
    }
}

//TODO lib?

fun KtParameterOrValueParameter.isVarArg(): Boolean = when (this) {
    is KtParameterOrValueParameter.Property -> false
    is KtParameterOrValueParameter.ValueParameter -> parameter.isVarArg
}

fun KtParameterOrValueParameter.typeReference(): KtTypeReference? = when (this) {
    is KtParameterOrValueParameter.Property -> property.typeReference
    is KtParameterOrValueParameter.ValueParameter -> parameter.typeReference
}
