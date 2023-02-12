package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.base.bll.kotlin.*
import csense.kotlin.annotations.idea.*
//import csense.kotlin.annotations.idea.inspections.numbers.bll.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*


class QuickNumberRangeParameterCallInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "NumberRangeParameterCallInspector"
    }

    override fun getStaticDescription(): String {
        return """
            For when ranges are specifed for a given number but the parameter is not in that range
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String {
        return "For when ranges are specifed for a given number but the parameter is not in that range"
    }

    override fun getShortName(): String {
        return "NumberRangeParameterCallInspector"
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
    ): KtVisitorVoid = callExpressionVisitor { ourCall ->
        val function = ourCall.resolveMainReferenceAsKtFunction() ?: return@callExpressionVisitor
//        function.validateNumberRangeForCallArguments(
//            forCall = ourCall,
//            holder = holder
//        )
    }
}
