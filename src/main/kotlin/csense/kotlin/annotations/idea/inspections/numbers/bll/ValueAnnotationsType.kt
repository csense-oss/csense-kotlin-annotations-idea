package csense.kotlin.annotations.idea.inspections.numbers.bll

import com.intellij.codeInspection.*
import csense.idea.base.bll.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.kotlin.models.*
import org.jetbrains.kotlin.builtins.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.typeUtil.*


data class ValueAnnotationsType(
    val annotations: List<KtAnnotationEntry>,
    val type: KotlinType?,
    val typeReference: KtTypeReference?,
    val isVarArg: Boolean,
    val valueExpressions: List<KtExpression>
)

fun ValueAnnotationsType.validateOrReportTo(holder: ProblemsHolder) {

    //vararg / list of lambda's!?s ........ :(
    val isNumberLambda = type?.isFunctionType == true
    if (isNumberLambda) {
        valueExpressions.forEach { expression ->
            val lambdaError: String? = typeReference?.validateLambdaOrError(expression)
            if (lambdaError != null) {
                holder.registerProblemHighlightElement(
                    psiElement = expression,
                    descriptionTemplate = lambdaError
                )
            }
        }


    }


    val isVarArgAndPrimitiveNumber = isVarArg && type?.isPrimitiveNumberArray() == true
    val isNumber = type?.isPrimitiveNumberOrNullableType() == true

    if (!isNumber && !isVarArgAndPrimitiveNumber) {
        return
    }
    val rangeParser = RangeParser.parseKt(annotations) ?: return

    valueExpressions.forEach { valueExpression ->
        val errorText = rangeParser.validateOrError(
            annotations = annotations,
            mayBeNull = type?.isMarkedNullable == true,
            valueExpression = valueExpression
        ) ?: return@forEach

        holder.registerProblemHighlightElement(
            psiElement = valueExpression,
            descriptionTemplate = errorText
        )
    }
}

//fun ValueAnnotationsType.validateLambdaOrReportTo(holder: ProblemsHolder) {
//    //TODO ...
//
//}


fun ParameterToValueExpression.toValueAnnotationsType(): ValueAnnotationsType = ValueAnnotationsType(
    annotations = allAnnotations,
    type = parameter.resolveType(),
    typeReference = parameter.typeReference,
    isVarArg = parameter.isVarArg,
    valueExpressions = valueArguments,
)


fun KotlinType.isPrimitiveNumberArray(): Boolean = nameAsString() in setOf(
    "ByteArray",
    "CharArray",
    "ShortArray",
    "IntArray",
    "LongArray",
    "FloatArray",
    "DoubleArray",
)


fun KotlinType.name() = this.constructor.declarationDescriptor?.name
fun KotlinType.nameAsString() = this.constructor.declarationDescriptor?.name?.asString()

fun KtTypeReference.validateLambdaOrError(valueExpressions: KtExpression): String? {
    val typeElement = typeElement as? KtFunctionType ?: return null
    val argument = valueExpressions as? KtLambdaExpression ?: return null

    val returnAnnotations = typeElement.returnTypeReference?.annotationEntries
    val parameterAnnotations = typeElement.parameters.map {
        ValueAnnotationsType(it.annotationEntries, it.resolveType(), it.typeReference, it.isVarArg)
    }



    parameterAnnotations.map {
    }

    //TODO .. this might be the  wrong place......




    return null//"failed to parse lambda signature and expression(s)"
}
