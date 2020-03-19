package csense.kotlin.annotations.idea.analyzers.noEscape

import com.intellij.codeInsight.*
import csense.idea.base.annotationss.*
import csense.idea.base.bll.psi.*
import csense.kotlin.annotations.idea.analyzers.*
import csense.kotlin.annotations.idea.inspections.sideeffect.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.uast.*

object NoEscapeAssignmentAnalyzer : Analyzer<KtExpression> {
    //Types of escape:
    // - direct assignment (simple) //either via a function call or object access.
    // - parsing as argument to function where its not marked NoEscape (as that means its allowed to escape) (semi difficult)
    // - for .let, apply ect we should inspect the lambda.. which can get quite tricky. (hard)
    // -
    
    override fun analyze(item: KtExpression): AnalyzerResult {
        val errors = mutableListOf<AnalyzerError>()
        val extManager = ExternalAnnotationsManager.getInstance(item.project)
        when (item) {
            is KtProperty -> {
                val isAnyNoEscape = item.initializer?.resolveAnnotationsKt(extManager)?.anyNoEscape() ?: false
                if (isAnyNoEscape) {
                    errors.add(AnalyzerError(
                            item,
                            "This is marked NoEscape; assignment prohibitemed.",
                            arrayOf()
                    ))
                }
            }
            is KtReturnExpression -> {
                val isAnyNoEscape = item.returnedExpression?.resolveAnnotationsKt(extManager)?.anyNoEscape() ?: false
                val method = item.findParentOfType<KtFunction>()
                val doesHaveAnnotation = method?.resolveAnnotationsKt(extManager)?.anyNoEscape() ?: false
                if (isAnyNoEscape && doesHaveAnnotation.not()) {
                    //quickfix : annotate method
                    errors.add(AnalyzerError(
                            item,
                            "This is marked NoEscape; Without annotating this as NoEscape you are escaping a NoEscape.",
                            arrayOf()
                    ))
                }
            }
        }
        return AnalyzerResult(errors)
    }
}


fun List<UAnnotation>.anyNoEscape(): Boolean = any {
    it.qualifiedName in NoEscapeAnnotationsFqNames
}

val NoEscapeAnnotationsFqNames = setOf("csense.kotlin.annotations.sideEffect.NoEscape")