package csense.kotlin.annotations.idea.inspections

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import csense.kotlin.annotations.idea.Constants
import csense.kotlin.annotations.idea.bll.RangeParser
import csense.kotlin.annotations.idea.quickfixes.ReverseRangeQuickFixKt
import csense.kotlin.annotations.idea.quickfixes.ReverseRangeQuickFixUart
import csense.kotlin.extensions.mapLazy
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.psi.annotationEntryVisitor
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.toUElementOfType

//instead of looking at the value of the range as the quick number range inspection does,
// this looks that the types matches up
//so eg, its not "@DoubleLimit" on an int.. ect
//or the values are fucked akk
//@IntRange(from = 1, to = 0) // quickfix inverse them ?
//or no valid range :
//@IntRange(from = 0, to = 0) //???

class QuickNumberRangeTypeValueInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "NumberRangeTypeValueInspector"
    }

    override fun getStaticDescription(): String? {
        return """
            
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String? {
        return "more desc ? "
    }

    override fun getShortName(): String {
        return "NumberRangeTypeValueInspector"
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

    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean): KtVisitorVoid {
        return annotationEntryVisitor {
            val asUAnnotation = it.toUElementOfType<UAnnotation>()
            if (asUAnnotation == null) {
                val asList = listOf(it)
                val type = RangeParser.parseKt(asList) ?: return@annotationEntryVisitor
                val isRightType = (it as? KtParameter)?.let { type.verifyTypeName(it) }
                if (isRightType != null && !isRightType) {
                    holder.registerProblem(it, "WRONG RANGE TYPE")
                    return@annotationEntryVisitor
                }
                val errorMessage = type.computeInvalidRangeMessageKt(asList)
                val isReversed = type.isRangeReveresedKt(asList)
                if (errorMessage != null) {
                    val quickFixes: Array<LocalQuickFix> = isReversed.mapLazy(ifTrue = {
                        arrayOf(ReverseRangeQuickFixKt(asList))
                    }, ifFalse = { arrayOf() })
                    holder.registerProblem(
                            it,
                            errorMessage,
                            *quickFixes
                    )
                }
            } else {
                val asList = listOf(asUAnnotation)
                val type = RangeParser.parse(asList) ?: return@annotationEntryVisitor
                val isRightType = (it as? KtParameter)?.let { type.verifyTypeName(it) }
                if (isRightType != null && !isRightType) {
                    holder.registerProblem(it, "WRONG RANGE TYPE")
                    return@annotationEntryVisitor
                }
                val errorMessage = type.computeInvalidRangeMessage(asList)
                val isReversed = type.isRangeReveresed(asList)
                if (errorMessage != null) {
                    val quickFixes: Array<LocalQuickFix> = isReversed.mapLazy(ifTrue = {
                        arrayOf(ReverseRangeQuickFixUart(asList))
                    }, ifFalse = { arrayOf() })
                    holder.registerProblem(
                            it,
                            errorMessage,
                            *quickFixes
                    )
                }
            }

        }
    }
}