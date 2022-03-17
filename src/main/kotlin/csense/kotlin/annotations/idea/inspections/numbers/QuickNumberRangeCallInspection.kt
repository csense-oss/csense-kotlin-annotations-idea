package csense.kotlin.annotations.idea.inspections.numbers

import com.intellij.codeHighlighting.*
import com.intellij.codeInsight.*
import com.intellij.codeInspection.*
import com.intellij.psi.*
import csense.idea.base.annotations.*
import csense.idea.base.bll.*
import csense.idea.base.bll.kotlin.*
import csense.kotlin.annotations.idea.*
import csense.kotlin.annotations.idea.analyzers.noEscape.*
import csense.kotlin.annotations.idea.bll.*
import org.jetbrains.kotlin.asJava.elements.*
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.idea.caches.resolve.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.resolve.lazy.*
import org.jetbrains.kotlin.resolve.lazy.descriptors.*
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.typeUtil.*
import org.jetbrains.uast.*


class QuickNumberRangeCallInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "NumberRangeInspector"
    }

    override fun getStaticDescription(): String? {
        return """
            
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String? {
        return "more desc ? "
    }

    override fun getShortName(): String {
        return "NumberRangeInspection"
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
        return callExpressionVisitor { ourCall ->
            //we look at value arguments
            val haveAnyNumbers = ourCall.anyDescendantOfType<KtConstantExpression> {
                it.resolveType()?.isPrimitiveNumberOrNullableType() == true
            }
            if (!haveAnyNumbers) {
                return@callExpressionVisitor //we do not track fully.that would not be fast.
            }

            val resolvedFunction = ourCall.resolveMainReference() ?: return@callExpressionVisitor
            val annotations: List<List<UAnnotation?>> = resolvedFunction.resolveAllParameterAnnotations()
            if (annotations.isEmpty()) {
                return@callExpressionVisitor
            }
            ourCall.analyzeUannotationValueArguments(annotations, holder)
        }
    }


    fun KtCallExpression.analyzeUannotationValueArguments(
        resolvedAnnotations: List<List<UAnnotation?>>,
        holder: ProblemsHolder
    ) {
        valueArguments.forEachIndexed { index: Int, ktValueArgument: KtValueArgument? ->
            val argAnnotations = resolvedAnnotations.getOrNull(index)
            if (argAnnotations != null && argAnnotations.isNotEmpty() && ktValueArgument != null) {
                val typeRange: RangeParser<*>? = RangeParser.parse(argAnnotations)
                if (typeRange != null && !typeRange.isValid(argAnnotations, ktValueArgument)) {
                    holder.registerProblemSafe(
                        ktValueArgument,
                        typeRange.computeErrorMessage(argAnnotations, ktValueArgument)
                    )
                }
            }
        }
    }
}

fun PsiElement.resolveAllParameterAnnotations(externalAnnotationsManager: ExternalAnnotationsManager? = null): List<List<UAnnotation>> {
    val extManager = externalAnnotationsManager ?: ExternalAnnotationsManager.getInstance(project)
    return when (this) {
        is KtParameter -> {
            if (this.typeReference?.isFunctional() == true) {
                val resolvedType = this.resolveType() ?: return emptyList()
                val params = resolvedType.resolveFunctionInputValueParameters()
                return params.map { it.resolveAnnotation() }
            } else {
                //resolve function annotations?
                emptyList()
            }
        }
        is KtLightMethod -> this.reference?.resolve()?.resolveAllParameterAnnotations(externalAnnotationsManager)
            ?: emptyList()
        is KtFunction -> valueParameters.getAllAnnotations(extManager)
        is PsiMethod -> parameterList.getAllAnnotations(extManager)
        else -> emptyList()
    }
}


fun KotlinType.resolveFunctionInputValueParameters(): List<TypeProjection> {
    if (arguments.size <= 1) {
        return emptyList()
    }
    return arguments.take(arguments.size - 1)
}

fun TypeProjection.resolveAnnotation(): List<UAnnotation> {
    return this.type.annotations.mapNotNull {
        val annotationDescription = it as? LazyAnnotationDescriptor
        annotationDescription?.annotationEntry.toUElement(UAnnotation::class.java)
    }
}

fun KtExpression.resolveType(): KotlinType? = this.analyze(BodyResolveMode.PARTIAL).getType(this)