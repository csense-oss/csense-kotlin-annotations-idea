package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.base.UastKtPsi.*
import csense.idea.base.annotations.*
import csense.idea.base.bll.*
import csense.idea.base.bll.kotlin.*
import csense.kotlin.annotations.idea.*
import csense.kotlin.annotations.idea.bll.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.uast.*


class QuickNumberRangeInspection : AbstractKotlinInspection() {
    
    override fun getDisplayName(): String {
        return "NumberRangeInspector"
    }
    
    override fun getStaticDescription(): String? {
        return """
            
        """.trimIndent()
    }
    
    override fun getDescriptionFileName(): String? {
        return "more desc ? "
    }
    
    override fun getShortName(): String {
        return "NumberRangeInspection"
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
        return callExpressionVisitor { ourCall ->
            //we look at value arguments
            val haveAnyNumbers = ourCall.anyDescendantOfType<KtConstantExpression> { it.isNumberType() }
            if (!haveAnyNumbers) {
                return@callExpressionVisitor //we do not track fully.that would not be fast.
            }
            
            
            val resolvedFunction = ourCall.resolvePsi() ?: return@callExpressionVisitor
            val annotations: List<List<UAnnotation?>> = resolvedFunction.resolveAllParameterAnnotations()
            if (annotations.isEmpty()) {
                return@callExpressionVisitor
            }
            ourCall.analyzeUannotationValueArguments(annotations, holder)
        }
    }
    
    
    fun KtCallExpression.analyzeUannotationValueArguments(
            resolvedAnnotations: List<List<UAnnotation?>>,
            holder: ProblemsHolder
    ) {
        valueArguments.forEachIndexed { index: Int, ktValueArgument: KtValueArgument? ->
            val argAnnotations = resolvedAnnotations.getOrNull(index)
            if (argAnnotations != null && argAnnotations.isNotEmpty() && ktValueArgument != null) {
                val typeRange: RangeParser<*>? = RangeParser.parse(argAnnotations)
                if (typeRange != null && !typeRange.validate(argAnnotations, ktValueArgument)) {
                    holder.registerProblemSafe(
                            ktValueArgument,
                            typeRange.computeErrorMessage(argAnnotations, ktValueArgument)
                    )
                }
            }
        }
    }
}
