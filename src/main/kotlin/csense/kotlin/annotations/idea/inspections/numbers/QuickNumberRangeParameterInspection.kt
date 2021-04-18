package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.base.UastKtPsi.*
import csense.idea.base.annotations.*
import csense.idea.base.bll.*
import csense.idea.base.bll.kotlin.*
import csense.kotlin.annotations.idea.*
import csense.kotlin.annotations.idea.bll.*
import csense.kotlin.extensions.collections.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.types.typeUtil.*
import org.jetbrains.uast.*
import kotlin.collections.getOrNull
import kotlin.collections.isNotEmpty


class QuickNumberRangeParameterInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "NumberFunctionRangeInspector"
    }

    override fun getStaticDescription(): String {
        return """
            
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String {
        return "more desc ? "
    }

    override fun getShortName(): String {
        return "NumberFunctionRangeInspection"
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
        return namedFunctionVisitor { function ->
            //we look at value arguments
            //TODO better test for numbers
            val numberParameters =
                function.valueParameters.filter {
                    it.resolveType()?.isPrimitiveNumberOrNullableType() == true &&
                            it.hasDefaultValue()
                }
            if (numberParameters.isEmpty()) {
                return@namedFunctionVisitor //we do not track fully.that would not be fast.
            }


            val annotations: List<List<UAnnotation?>> = function.resolveAllParameterAnnotations()
            if (annotations.isEmpty()) {
                return@namedFunctionVisitor
            }
            numberParameters.forEach {
                val index = it.parameterIndex()

                if (!annotations.isIndexValid(index)) {
                    return@forEach

                }
                val annotationsForParam = annotations[index]
                if (annotationsForParam.isEmpty()) {
                    return@forEach
                }
                validateAnnotationAndDefaultValue(it, annotationsForParam, holder)
            }
        }
    }

    fun validateAnnotationAndDefaultValue(param: KtParameter, annotations: List<UAnnotation?>, holder: ProblemsHolder) {
        val typeRange = RangeParser.parse(annotations) ?: return
        val expression = param.defaultValue ?: return
        val isInvalid = !typeRange.validate(annotations, expression)
        if (isInvalid) {
            holder.registerProblemSafe(
                param,
                typeRange.computeErrorMessage(annotations, expression)
            )
        }
    }

}
