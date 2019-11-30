package csense.kotlin.annotations.idea.inspections

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.util.parentOfType
import csense.kotlin.annotations.idea.Constants
import csense.kotlin.annotations.idea.psi.resolveAllMethodAnnotations
import org.jetbrains.kotlin.codegen.coroutines.createMethodNodeForSuspendCoroutineUninterceptedOrReturn
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.nj2k.nullabilityAnalysis.prepareTypeElementByMakingAllTypesNullable
import org.jetbrains.kotlin.psi.*
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.toUElementOfType

class NoEscapeAssigmentInspection : AbstractKotlinInspection() {
    override fun getDisplayName(): String {
        return "NoEscapeAssignmentInspection"
    }

    override fun getStaticDescription(): String? {
        return """
            
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String? {
        return "more desc ? "
    }

    override fun getShortName(): String {
        return "NoEscapeAssignmentInspection"
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
        //Types of escape:
        // - direct assignment (simple) //either via a function call or object access.
        // - parsing as argument to function where its not marked NoEscape (as that means its allowed to escape) (semi difficult)
        // - for .let, apply ect we should inspect the lambda.. which can get quite tricky. (hard)
        // -
        return expressionVisitor {
            val extManager = ExternalAnnotationsManager.getInstance(it.project)
            when (it) {
                is KtProperty -> {
                    val isAnyNoEscape = it.initializer?.resolveAnnotationsKt(extManager)?.anyNoEscape() ?: false
                    if (isAnyNoEscape) {
                        holder.registerProblem(it, "This is marked NoEscape; assignment prohibited.")
                    }
                }
                is KtReturnExpression -> {
                    val isAnyNoEscape = it.returnedExpression?.resolveAnnotationsKt(extManager)?.anyNoEscape() ?: false
                    val method = it.findParentOfType<KtFunction>()
                    val doesHaveAnnotation = method?.resolveAnnotationsKt(extManager)?.anyNoEscape() ?: false
                    if (isAnyNoEscape && doesHaveAnnotation.not()) {
                        //quickfix : annotate method
                        holder.registerProblem(it,
                                "This is marked NoEscape; without annotating this as NoEscape you are escaping a NoEscape.")
                    }
                }
            }
        }

    }
}

inline fun <reified T> KtExpression.findParentOfType(limit: PsiElement? = null): T? {
    var currentParent: PsiElement? = this
    while (currentParent != null && currentParent != limit) {
        if (currentParent is T) {
            return currentParent
        }
        currentParent = currentParent.parent
    }
    return null
}

fun List<UAnnotation>.anyNoEscape(): Boolean {
    return any { it.qualifiedName in NoEscapeAnnotationsFqNames }
}

fun KtExpression.resolveAnnotationsKt(extManager: ExternalAnnotationsManager): List<UAnnotation> = when (this) {
    is KtCallExpression -> resolvePsi()?.resolveAllMethodAnnotations(extManager)
    is KtProperty -> (getter ?: initializer)?.resolveAllMethodAnnotations(extManager)?.plus(uAnnotaions())
    is KtDotQualifiedExpression -> selectorExpression?.resolveAnnotationsKt(extManager)
    is KtNameReferenceExpression -> this.references.firstOrNull()?.resolve()?.resolveAnnotations(extManager)
    is KtNamedFunction -> resolveAllMethodAnnotations()
    else -> emptyList()
} ?: emptyList()

fun PsiElement.resolveAnnotations(extManager: ExternalAnnotationsManager): List<UAnnotation> {
    return when (this) {
        is KtExpression -> resolveAnnotationsKt(extManager)
        is PsiMethod -> resolveAllMethodAnnotations(extManager)
        is PsiField -> this.uAnnotations()
        else -> emptyList()
    }
}

fun KtAnnotated.uAnnotaions(): List<UAnnotation> {
    return annotationEntries.mapNotNull { it.toUElementOfType<UAnnotation>() }
}

fun PsiModifierListOwner.uAnnotations(): List<UAnnotation> {
    return annotations.mapNotNull { it.toUElementOfType<UAnnotation>() }
}


val NoEscapeAnnotationsFqNames = setOf("csense.kotlin.annotations.sideEffect.NoEscape")