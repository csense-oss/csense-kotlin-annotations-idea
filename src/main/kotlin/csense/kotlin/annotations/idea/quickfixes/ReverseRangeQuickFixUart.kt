package csense.kotlin.annotations.idea.quickfixes

import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.util.*
import csense.kotlin.annotations.idea.*
//import csense.kotlin.annotations.idea.inspections.numbers.bll.*
import org.jetbrains.kotlin.idea.util.application.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.uast.*
//
//class ReverseRangeQuickFixUart(
//        val elements: List<UAnnotation?>
//) : LocalQuickFix {
//
//    override fun getFamilyName() = "${Constants.quickFixFamilyPrefix} swap range (from & to)"
//    override fun getName(): String = "Swap range from and to"
//
//    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
//        val ranges = RangeParser.parse(elements) ?: return
//        //sanity test.
//        if (!ranges.isRangeReveresed(elements)) {
//            return
//        }
//        val annotation = ranges.findAnnotation(elements) ?: return
//        val values = annotation
//                .sourcePsi
//                ?.collectDescendantsOfType<KtConstantExpression>()
//                ?: return
//        if (values.size != 2) {
//            return
//        }
//        //swap args
//        project.executeWriteCommand("Swap Args") {
//            try {
//                values.first().replace(values.last().copy())
//                values.last().replace(values.first().copy())
//            } catch (e: IncorrectOperationException) {
//                TODO("Add error handling here")
//            }
//        }
//    }
//
//    override fun startInWriteAction(): Boolean = false
//}