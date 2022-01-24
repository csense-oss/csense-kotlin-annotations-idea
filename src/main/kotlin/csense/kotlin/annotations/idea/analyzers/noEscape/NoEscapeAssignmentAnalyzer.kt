package csense.kotlin.annotations.idea.analyzers.noEscape

import com.intellij.codeInsight.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.*
import com.intellij.psi.search.*
import csense.idea.base.annotations.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.*
import csense.idea.base.uastKtPsi.*
import csense.kotlin.annotations.idea.analyzers.*
import org.jetbrains.kotlin.lexer.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
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
            is KtBinaryExpression -> {
                //say an assignment of an escape thing
                val isAssignment = item.operationToken == KtTokens.EQ
                if (isAssignment) {
                    if (item.right?.resolveAnnotations(extManager)?.anyNoEscape() == true) {
                        errors.add(
                            AnalyzerError(
                                item.right ?: item,
                                "Assigning something marked NoEscape means it escapes",
                                arrayOf()
                            )
                        )
                    }
                }
            }
            is KtProperty -> {
                val isAnyNoEscape = item.initializer?.resolveAnnotationsKt(extManager)?.anyNoEscape() ?: false
                if (isAnyNoEscape) {
                    errors.add(
                        AnalyzerError(
                            item,
                            "This is marked NoEscape; assignment prohibited.",
                            arrayOf()
                        )
                    )
                }
            }
            is KtCallExpression -> {
                item.foreachParameterWithResolvedArgument { argument, resolvedParam ->
                    val argHasNoEscape = argument.resolveAnnotations(extManager).anyNoEscape()
                    if (argHasNoEscape) {
                        val paramHasNoEscape = resolvedParam.resolveAnnotations(extManager).anyNoEscape()
                        if (!paramHasNoEscape) {
                            errors.add(
                                AnalyzerError(
                                    argument,
                                    "Argument marked as NoEscape but is escaping via parameter ${resolvedParam.name} since it is not marked as NoEscape",
                                    arrayOf()
                                )
                            )
                        }
                    }
                }
            }
            is KtReturnExpression -> {
                val isAnyNoEscape = item.returnedExpression?.resolveAnnotationsKt(extManager)?.anyNoEscape() ?: false
                val method = item.findParentOfType<KtFunction>()
                val doesHaveAnnotation = method?.resolveAnnotationsKt(extManager)?.anyNoEscape() ?: false
                if (isAnyNoEscape && doesHaveAnnotation.not()) {
                    //quickfix : annotate method
                    errors.add(
                        AnalyzerError(
                            item,
                            "This is marked NoEscape; Without annotating this as NoEscape you are escaping a NoEscape.",
                            arrayOf()
                        )
                    )
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


//TODO Base module
//---------- attempt at "function invocation parameter resolution" akk matching called arg with resolved function arg.

//example
fun x(a: Int, b: Char) {

}

fun useX() {
    x(42, 'a')
}

fun KtCallExpression.foreachParameterWithResolvedArgument(onParameter: (KtValueArgument, KtParameter) -> Unit) {
    val resolvedFunction = resolveRealType(false) as? KtFunction ?: return

    this.valueArguments.forEachIndexed { index, param ->
        //find index .. TODO this is not valid always
        val resolvedParm = resolvedFunction.valueParameters[index]
        onParameter(param, resolvedParm)
    }

}

@OptIn(ExperimentalStdlibApi::class)
fun <U> KtCallExpression.mapParameterWithResolvedArgument(mapOnParameter: (KtValueArgument, KtParameter) -> U): List<U> {
    return buildList {
        foreachParameterWithResolvedArgument { arg, resolvedParam ->
            this += mapOnParameter(arg, resolvedParam)
        }
    }
}

//expected "usage" / code for working with it


//fun inspectX(call: KtCallExpression){
//    call.resolveRealType()?.foreachParameterWithResolvedArgument { parameter, resolvedArg ->
//
//    }
//}

//----------


fun PsiElement.resolveRealType(shouldResolveNested: Boolean): PsiElement? {
    return when (this) {
        is KtElement -> resolveRealType(shouldResolveNested)
        is PsiClass -> this
        is PsiMethod -> {
            if (this.isConstructor) {
                this.containingClass
            } else {
                (returnType as? PsiClassReferenceType)?.resolve()
            }

        }
        is PsiReference -> references.firstOrNull()?.resolve()
        //TODO improve?
        is PsiField -> {
            val project = project
            return JavaPsiFacade.getInstance(project)
                .findClass(this.type.canonicalText, this.type.resolveScope ?: GlobalSearchScope.allScope(project))
        }
        else -> this
    }
}

fun KtProperty.resolveRealType(shouldResolveNested: Boolean): PsiElement? {
    val type = typeReference
    if (type != null) {
        return type.resolve()?.resolveRealType(shouldResolveNested)
    }
    val init = initializer
    if (init != null) {
        return init.resolveRealType(shouldResolveNested)
    }
    val getter = getter
    if (getter != null) {
        return getter.resolveRealType(shouldResolveNested)
    }
    return this
}

tailrec fun KtElement.resolveRealType(shouldResolveNested: Boolean): PsiElement? {
    return when (this) {
        is KtCallExpression -> {
            val ref = resolveMainReferenceWithTypeAlias()
            ref?.resolveRealType(shouldResolveNested)
        }
        is KtDotQualifiedExpression -> rightMostSelectorExpression()?.resolveRealType(shouldResolveNested)
        is KtProperty -> resolveRealType(shouldResolveNested)
        is KtSecondaryConstructor, is KtPrimaryConstructor -> {
            this.containingClass()
        }

        is KtNamedFunction -> {
            if (shouldResolveNested) {
                this.getDeclaredReturnType()
            } else {
                this
            }
        }
        is KtParameter -> {
            if (this.isFunctionalType()) {
                //well this is it.. we cannot go any deeper..
                this
            } else {
                if (shouldResolveNested) {
                    resolveRealType(shouldResolveNested)
                } else {
                    this
                }
            }

        }
        is KtValueArgument -> getArgumentExpression()?.resolveRealType(shouldResolveNested)
        is KtCallableReferenceExpression -> callableReference.resolveRealType(shouldResolveNested)
        //TODO should be "first" non null instead of assuming the first is the right one?
        is KtReferenceExpression -> {
            val ref = references.firstOrNull()
            ref?.resolveRealType(shouldResolveNested)
        }
        else -> this
    }
}

fun PsiReference.resolveRealType(shouldResolveNested: Boolean): PsiElement? {
    return resolve()?.resolveRealType(shouldResolveNested)
}


fun KtExpression.resolveAnnotationsKt(extManager: ExternalAnnotationsManager): List<UAnnotation> = when (this) {
    is KtCallExpression -> resolveRealType(false)?.resolveAllMethodAnnotations(extManager)
    is KtProperty -> (getter ?: initializer)?.resolveAllMethodAnnotations(extManager)?.plus(uAnnotaions())
    is KtDotQualifiedExpression -> selectorExpression?.resolveAnnotationsKt(extManager)
    is KtNameReferenceExpression -> this.references.firstOrNull()?.resolve()?.resolveAnnotations(extManager)
    is KtNamedFunction -> resolveAllMethodAnnotations()
    is KtParameter -> annotationEntries.mapNotNull { it.toUElement(UAnnotation::class.java) }
    is ValueArgument -> getArgumentExpression()?.resolveAnnotations(extManager)
    else -> emptyList()
} ?: emptyList()

fun PsiElement.resolveAnnotations(extManager: ExternalAnnotationsManager): List<UAnnotation> {
    return when (this) {
        is KtExpression -> resolveAnnotationsKt(extManager)
        is ValueArgument -> getArgumentExpression()?.resolveAnnotations(extManager) ?: emptyList()
        is PsiMethod -> resolveAllMethodAnnotations(extManager)
        is PsiField -> this.uAnnotations()
        else -> emptyList()
    }
}

