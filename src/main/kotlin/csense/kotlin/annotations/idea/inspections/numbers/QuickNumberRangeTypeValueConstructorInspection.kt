package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.visitors.*
import csense.kotlin.annotations.idea.*
//import csense.kotlin.annotations.idea.inspections.numbers.bll.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*

class QuickNumberRangeTypeValueConstructorInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "NumberRangeTypeConstructorValueInspector"
    }

    override fun getStaticDescription(): String {
        return """
            Validates number ranges for constructors
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String {
        return "Validates number ranges for constructors"
    }

    override fun getShortName(): String {
        return "NumberRangeTypeConstructorValueInspector"
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
    ): KtVisitorVoid = ConstructorVisitor {
//        it.valueParameters.validateValueParameters(holder)
//        if (it is KtSecondaryConstructor) {
//            it.getDelegationCallOrNull()?.validateDelegationCall(holder)
//        }
    }
}
