package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*

import csense.idea.base.bll.*
import csense.kotlin.annotations.idea.*
import csense.kotlin.annotations.idea.bll.*
import csense.kotlin.annotations.idea.inspections.numbers.bll.*
import csense.kotlin.extensions.collections.*
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.idea.caches.resolve.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.typeUtil.*
import org.jetbrains.uast.*


class QuickNumberRangeDefaultParameterInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "NumberFunctionRangeDefaultValueInspector"
    }

    override fun getStaticDescription(): String {
        return """
            Validates if there are limits on a given parameter that default arguments obey them.
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String {
        return "Validates if there are limits on a given parameter that default arguments obey them."
    }

    override fun getShortName(): String {
        return "NumberFunctionRangeDefaultValueInspector"
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
            val validArguments: List<Pair<KtParameter, List<UAnnotation>>> = function.parametersWithAnnotations(
                filter = { parameter, annotations ->
                    parameter.isNumberType()
                }
            )


            validArguments.forEach {
                validateAnnotationAndDefaultValue(param = it.first, annotations = it.second, holder = holder)
            }
        }
    }

    fun validateAnnotationAndDefaultValue(param: KtParameter, annotations: List<UAnnotation?>, holder: ProblemsHolder) {
        val typeRange = RangeParser.parse(annotations) ?: return
        val expression = param.defaultValue ?: return
        val isInvalid = !typeRange.isValid(annotations, expression)
        if (isInvalid) {
            val potentialErrorMessage = typeRange.computeErrorMessage(annotations, expression)
            if (potentialErrorMessage != null) {
                holder.registerProblemSafe(
                    psiElement = param,
                    descriptionTemplate = potentialErrorMessage
                )
            }
        }
    }


}
