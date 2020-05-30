package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import csense.idea.base.bll.psi.*
import csense.kotlin.annotations.idea.Constants
import csense.kotlin.annotations.idea.bll.RangeParser
import csense.kotlin.annotations.idea.quickfixes.ReverseRangeQuickFixKt
import csense.kotlin.extensions.collections.list.*
import csense.kotlin.extensions.mapLazy
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
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
    
    //TODO what the actuall is this... it tests the args of the annotations, not the type its "on". ?
    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean
    ): KtVisitorVoid {
        
        return annotationEntryVisitor {
            val asList = listOf(it)
            val annotationType = RangeParser.parseKt(asList) ?: return@annotationEntryVisitor
            
            val parm = it.findParentOfType<KtParameter>() ?: return@annotationEntryVisitor
            val resolvedType = parm.typeReference?.text ?: return@annotationEntryVisitor
            if (annotationType.allowedTypeNames.doesNotContain(resolvedType)) {
                holder.registerProblem(it, "Wrong range type. Expected `${annotationType.allowedTypeNames}` but got `$resolvedType`")
                return@annotationEntryVisitor
            }
            
            val errorMessage = annotationType.computeInvalidRangeMessageKt(asList)
            val isReversed = annotationType.isRangeReveresedKt(asList)
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
//
//            val asUAnnotation = it.toUElementOfType<UAnnotation>()
//            if (asUAnnotation == null) {
//                val asList = listOf(it)
//                val type = RangeParser.parseKt(asList) ?: return@annotationEntryVisitor
//                val isAnyWrongType = it.valueArguments.any { valueArg ->
//                    valueArg.getArgumentExpression()?.let { parm -> !type.verifyTypeName(parm) } ?: false
//                }
//                if (isAnyWrongType) {
//                    holder.registerProblem(it, "Wrong range type. Expected ${type.typeName}")
//                    return@annotationEntryVisitor
//                }
//                val errorMessage = type.computeInvalidRangeMessageKt(asList)
//                val isReversed = type.isRangeReveresedKt(asList)
//                if (errorMessage != null) {
//                    val quickFixes: Array<LocalQuickFix> = isReversed.mapLazy(ifTrue = {
//                        arrayOf(ReverseRangeQuickFixKt(asList))
//                    }, ifFalse = { arrayOf() })
//                    holder.registerProblem(
//                            it,
//                            errorMessage,
//                            *quickFixes
//                    )
//                }
//            } else {
//
//                val asList = listOf(asUAnnotation)
//                val type = RangeParser.parse(asList) ?: return@annotationEntryVisitor
//                val isAnyWrongType = !type.allowDifferentArgumentTypesThanAnnotating && it.valueArguments.any { valueArg ->
//                    valueArg.getArgumentExpression()?.let { parm -> !type.verifyTypeName(parm) } ?: false
//                }
//                if (isAnyWrongType) {
//                    holder.registerProblem(it, "Wrong range type. Expected ${type.typeName}")
//                    return@annotationEntryVisitor
//                }
//                val errorMessage = type.computeInvalidRangeMessage(asList)
//                val isReversed = type.isRangeReveresed(asList)
//                if (errorMessage != null) {
//                    val quickFixes: Array<LocalQuickFix> = isReversed.mapLazy(ifTrue = {
//                        arrayOf(ReverseRangeQuickFixUart(asList))
//                    }, ifFalse = { arrayOf() })
//                    holder.registerProblem(
//                            it,
//                            errorMessage,
//                            *quickFixes
//                    )
//                }
//            }
            
        }
    }
}