package csense.kotlin.annotations.idea.inspections.numbers.bll

import com.intellij.codeInspection.*
import csense.idea.base.bll.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.kotlin.models.*
import csense.kotlin.annotations.idea.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.typeUtil.*


data class ValueAnnotationsType(
    val annotations: List<KtAnnotationEntry>,
    val type: KotlinType?,
    val valueExpression: KtExpression
)

fun ValueAnnotationsType.validateOrReportTo(holder: ProblemsHolder) {
    val isNumber = type?.isPrimitiveNumberOrNullableType() == true
    if (!isNumber) {
        return
    }
    val rangeParser = RangeParser.parseKt(annotations) ?: return
    val errorText = rangeParser.validateOrError(
        annotations = annotations,
        mayBeNull = type?.isMarkedNullable == true,
        valueExpression = valueExpression
    ) ?: return

    holder.registerProblemHighlightElement(
        psiElement = valueExpression,
        descriptionTemplate = errorText
    )
}

fun ParameterToValueExpression.toValueAnnotationsType(): ValueAnnotationsType? {
    return ValueAnnotationsType(
        annotations = parameterAnnotations,
        type = parameter.resolveType(),
        valueExpression = valueArgument ?: return null
    )
}
