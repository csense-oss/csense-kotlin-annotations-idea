package csense.kotlin.annotations.idea.analyzers

import com.intellij.codeInspection.*
import com.intellij.psi.*
import csense.idea.base.bll.*


class AnalyzerError(
        val psiElement: PsiElement,
        val descriptionTemplate: String,
        val fixes: Array<LocalQuickFix>
)

fun ProblemsHolder.registerProblem(analyzeError: AnalyzerError) {
    registerProblemSafe(analyzeError.psiElement, analyzeError.descriptionTemplate, *analyzeError.fixes)
}

interface Analyzer<T : PsiElement> {
    fun analyze(item: T): AnalyzerResult
}

class AnalyzerResult(val errors: List<AnalyzerError>) {
    companion object {
        val empty = AnalyzerResult(listOf())
    }
}