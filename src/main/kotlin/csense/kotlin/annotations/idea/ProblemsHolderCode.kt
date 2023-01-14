package csense.kotlin.annotations.idea

import com.intellij.codeInspection.*
import com.intellij.psi.PsiElement


/**
 * Will report a problem and highlight the error'ed element rather han just underlining it.
 */
fun ProblemsHolder.registerProblemHighlightElement(
    psiElement: PsiElement,
    descriptionTemplate: String,
    fixes: Array<LocalQuickFix> = arrayOf()
) {
    val error: ProblemDescriptor = InspectionManager.getInstance(project).createProblemDescriptor(
        /* psiElement = */ psiElement,
        /* descriptionTemplate = */ descriptionTemplate,
        /* fixes = */ fixes,
        /* highlightType = */ ProblemHighlightType.ERROR,
        /* onTheFly = */ true,
        /* isAfterEndOfLine = */ false
    )
    registerProblem(error)
}