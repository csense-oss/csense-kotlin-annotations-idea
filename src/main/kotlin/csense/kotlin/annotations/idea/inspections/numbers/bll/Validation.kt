//package csense.kotlin.annotations.idea.inspections.numbers.bll
//
//import com.intellij.codeInspection.*
//import csense.idea.base.bll.*
//import csense.idea.base.bll.kotlin.*
//import org.jetbrains.kotlin.psi.*
//
//
//fun KtConstructorDelegationCall.validateDelegationCall(holder: ProblemsHolder) {
//    val calling = resolveConstructorCall() ?: return
//    calling.validateNumberRangeForCallArguments(forCall = this, holder = holder)
//}
//
//fun KtCallableDeclaration.validateNumberRangeForCallArguments(
//    forCall: KtCallElement,
//    holder: ProblemsHolder
//) {
//    parametersFromCallWithAnnotations(forCall) {
//        it.toValueAnnotationsType().validateOrReportTo(holder)
//        false
//    }
//}
//
//
//fun List<KtParameter>.validateValueParameters(
//    holder: ProblemsHolder
//) = forEach {
//    it.validateNumberRangeForDefaultValue(holder)
//}
//
//fun KtProperty.validateNumberRangeForInitializer(holder: ProblemsHolder) {
//    val initializerExpression = initalizerOrGetter() ?: return
//    validateNumberRangeFor(
//        expression = initializerExpression,
//        isVarArg = false,
//        typeReference = typeReference,
//        holder = holder
//    )
//}
//
//fun KtParameter.validateNumberRangeForDefaultValue(
//    holder: ProblemsHolder
//) {
//    validateNumberRangeFor(
//        expression = defaultValue ?: return,
//        isVarArg = isVarArg,
//        typeReference = typeReference,
//        holder = holder
//    )
//}
//
//fun KtDeclaration.validateNumberRangeFor(
//    expression: KtExpression,
//    isVarArg: Boolean,
//    typeReference: KtTypeReference?,
//    holder: ProblemsHolder
//) {
//    val info = ValueAnnotationsType(
//        annotations = annotationEntries,
//        type = resolveType(),
//        typeReference = typeReference,
//        parameterName = name,
//        isVarArg = isVarArg,
//        valueExpressions = listOf(expression)
//    )
//    info.validateOrReportTo(holder)
//}
//
//fun List<KtExpression>.validateConstantsFor(
//    annotations: List<KtAnnotationEntry>,
//    mayResultBeNull: Boolean,
//    holder: ProblemsHolder
//) {
//    val rangeParser = RangeParser.parseKt(annotations) ?: return
//    //TODO OPTIMIZE THIS...
////    val annotation = rangeParser.findAnnotationKt(annotations) ?: return
////    val range = annotation.asRangePair(rangeParser.minValue, rangeParser.maxValue, rangeParser.parseValueKt) ?: return
//
//    forEach {
//        val potentialError = rangeParser.validateOrError(
//            annotations = annotations,
//            mayBeNull = mayResultBeNull,
//            valueExpression = it
//        ) ?: return@forEach
//        holder.registerProblemHighlightElement(
//            psiElement = it,
//            descriptionTemplate = potentialError
//        )
//    }
//}