package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.kotlin.annotations.idea.*
//import csense.kotlin.annotations.idea.inspections.numbers.bll.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*


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
    ): KtVisitorVoid = namedFunctionVisitor { function ->
//        function.valueParameters.validateValueParameters(holder)
    }
}

