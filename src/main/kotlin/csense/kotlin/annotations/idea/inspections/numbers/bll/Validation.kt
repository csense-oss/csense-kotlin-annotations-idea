package csense.kotlin.annotations.idea.inspections.numbers.bll

import com.intellij.codeInspection.*
import csense.idea.base.bll.kotlin.*
import org.jetbrains.kotlin.psi.*



fun KtConstructorDelegationCall.validateDelegationCall(holder: ProblemsHolder) {
    val calling = resolveConstructorCall() ?: return
    calling.validateNumberRangeForCallArguments(forCall = this, holder = holder)
}

fun KtCallableDeclaration.validateNumberRangeForCallArguments(
    forCall: KtCallElement,
    holder: ProblemsHolder
) {
    parametersFromCallWithAnnotations(forCall) {
        it.toValueAnnotationsType()?.validateOrReportTo(holder)
        false
    }
}


fun List<KtParameter>.validateValueParameters(
    holder: ProblemsHolder
) = forEach {
    it.validateNumberRangeForDefaultValue(holder)
}

fun KtProperty.validateNumberRangeForInitializer(holder: ProblemsHolder){
    val initializerExpression = initalizerOrGetter() ?: return
    validateNumberRangeFor(
        expression = initializerExpression,
        holder = holder
    )
}

fun KtParameter.validateNumberRangeForDefaultValue(
    holder: ProblemsHolder
) {
    validateNumberRangeFor(expression = defaultValue ?: return, holder = holder)
}

fun KtDeclaration.validateNumberRangeFor(
    expression: KtExpression,
    holder: ProblemsHolder
) {
    val info = ValueAnnotationsType(
        annotations = annotationEntries,
        type = resolveType(),
        valueExpression = expression
    )
    info.validateOrReportTo(holder)
}

