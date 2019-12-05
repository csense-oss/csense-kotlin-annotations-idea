package csense.kotlin.annotations.idea.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import csense.kotlin.annotations.idea.Constants
import csense.kotlin.annotations.idea.bll.RangeParser
import org.jetbrains.kotlin.idea.util.application.executeWriteCommand
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.tryResolve

class ReverseRangeQuickFixUart(
        val elements: List<UAnnotation?>) : LocalQuickFix {

    override fun getFamilyName() = "${Constants.quickFixFamilyPrefix} swap range (from & to)"
    override fun getName(): String = "Swap range from and to"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val ranges = RangeParser.parse(elements) ?: return
        //sanity test.
        if (!ranges.isRangeReveresed(elements)) {
            return
        }
        val annotation = ranges.findAnnotation(elements) ?: return
        val values = annotation
                .sourcePsi
                ?.collectDescendantsOfType<KtConstantExpression>()
                ?: return
        if (values.size != 2) {
            return
        }
        //swap args
        project.executeWriteCommand("swap args") {
            values.first().replace(values.last().copy())
            values.last().replace(values.first().copy())
        }
    }

    override fun startInWriteAction(): Boolean = false
}