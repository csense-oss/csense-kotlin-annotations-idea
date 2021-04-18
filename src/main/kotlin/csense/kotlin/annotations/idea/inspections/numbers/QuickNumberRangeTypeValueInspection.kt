package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.psi.*
import csense.idea.base.bll.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.*
import csense.kotlin.annotations.idea.*
import csense.kotlin.annotations.idea.bll.*
import csense.kotlin.annotations.idea.quickfixes.*
import csense.kotlin.extensions.*
import csense.kotlin.extensions.collections.list.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*

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

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): KtVisitorVoid {

        return annotationEntryVisitor {
            val asList = listOf(it)
            val annotationType = RangeParser.parseKt(asList) ?: return@annotationEntryVisitor

            val parm = it.findParentOfType<KtParameter>() ?: return@annotationEntryVisitor
            val resolvedType = parm.typeReference?.resolve()
                ?.invokeIsInstance<PsiNamedElement, String?> { element: PsiNamedElement -> element.name }
                ?: parm.resolveType()?.toString()

            val resolvedTypeNamed: String = resolvedType ?: return@annotationEntryVisitor
            if (annotationType.allowedTypeNames.doesNotContain(resolvedTypeNamed)) {
                holder.registerProblemSafe(
                    it,
                    "Wrong range type. Expected `${annotationType.allowedTypeNames}` but got `$resolvedTypeNamed`"
                )
                return@annotationEntryVisitor
            }

            val errorMessage = annotationType.computeInvalidRangeMessageKt(asList)
            val isReversed = annotationType.isRangeReveresedKt(asList)
            if (errorMessage != null) {
                val quickFixes: Array<LocalQuickFix> = isReversed.mapLazy(ifTrue = {
                    arrayOf(ReverseRangeQuickFixKt(asList))
                }, ifFalse = { arrayOf() })
                holder.registerProblemSafe(
                    it,
                    errorMessage,
                    *quickFixes
                )
            }
        }
    }
}
