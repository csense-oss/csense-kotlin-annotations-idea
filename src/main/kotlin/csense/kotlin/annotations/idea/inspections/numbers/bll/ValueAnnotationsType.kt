//package csense.kotlin.annotations.idea.inspections.numbers.bll
//
//import com.intellij.codeInspection.*
//import csense.idea.base.bll.*
//import csense.idea.base.bll.kotlin.*
//import csense.idea.base.bll.kotlin.models.*
//import org.jetbrains.kotlin.builtins.*
//import org.jetbrains.kotlin.psi.*
//import org.jetbrains.kotlin.types.*
//import org.jetbrains.kotlin.types.typeUtil.*
//
//
//data class ValueAnnotationsType(
//    val annotations: List<KtAnnotationEntry>,
//    val type: KotlinType?,
//    val typeReference: KtTypeReference?,
//    val parameterName: String?,
//    val isVarArg: Boolean,
//    val valueExpressions: List<KtExpression>
//)
//
//fun ValueAnnotationsType.validateOrReportTo(holder: ProblemsHolder) {
//
//    //vararg / list of lambda's!?s ........ :(
//    val isParameterLambda = type?.isFunctionType == true
//    if (isParameterLambda) {
//        valueExpressions.forEach { expression ->
//            typeReference?.validateLambdaReturns(expression, parameterName, holder)
//        }
//        return
//    }
//
//
//    val isVarArgAndPrimitiveNumber = isVarArg && type?.isPrimitiveNumberArray() == true
//    val isNumber = type?.isPrimitiveNumberOrNullableType() == true
//
//    if (!isNumber && !isVarArgAndPrimitiveNumber) {
//        return
//    }
//    val rangeParser = RangeParser.parseKt(annotations) ?: return
//
//    valueExpressions.forEach { valueExpression ->
//        val errorText = rangeParser.validateOrError(
//            annotations = annotations,
//            mayBeNull = type?.isMarkedNullable == true,
//            valueExpression = valueExpression
//        ) ?: return@forEach
//
//        holder.registerProblemHighlightElement(
//            psiElement = valueExpression,
//            descriptionTemplate = errorText
//        )
//    }
//}
//
////fun ValueAnnotationsType.validateLambdaOrReportTo(holder: ProblemsHolder) {
////    //TODO ...
////
////}
//
//
//fun ParameterToValueExpression.toValueAnnotationsType(): ValueAnnotationsType = ValueAnnotationsType(
//    annotations = allAnnotations,
//    type = parameter.resolveType(),
//    typeReference = parameter.typeReference,
//    parameterName = parameter.name,
//    isVarArg = parameter.isVarArg,
//    valueExpressions = valueArguments,
//)
//
//
//fun KotlinType.isPrimitiveNumberArray(): Boolean = nameAsString() in setOf(
//    "ByteArray",
//    "CharArray",
//    "ShortArray",
//    "IntArray",
//    "LongArray",
//    "FloatArray",
//    "DoubleArray",
//)
//
//
//fun KotlinType.name() = this.constructor.declarationDescriptor?.name
//fun KotlinType.nameAsString() = this.constructor.declarationDescriptor?.name?.asString()
//
//fun KtTypeReference.validateLambdaReturns(
//    valueExpressions: KtExpression,
//    parameterName: String?,
//    holder: ProblemsHolder
//) {
//    val typeElementOfParameter = typeElement as? KtFunctionType ?: return
//
//    val returnType = typeElementOfParameter.returnTypeReference ?: return
//
//    val returnAnnotationsForParameter = returnType.annotationEntries
//
//    val argumentLambda = valueExpressions as? KtLambdaExpression ?: return
//    val returnExpressions = argumentLambda.findAllReturnValueExpressions(
//        forName = parameterName
//    ) // TODO name?! argument name? hmm
//
//    val lastExpression = argumentLambda.bodyExpression?.lastChild
//
//
//    returnExpressions.validateConstantsFor(
//        annotations = returnAnnotationsForParameter,
//        mayResultBeNull = returnType.isNullableType(),
//        holder = holder
//    )
//}
