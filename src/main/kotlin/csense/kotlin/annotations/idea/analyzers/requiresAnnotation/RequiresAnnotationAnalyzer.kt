package csense.kotlin.annotations.idea.analyzers.requiresAnnotation

import com.intellij.codeInsight.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.*
import csense.idea.base.annotations.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.*
import csense.idea.base.uastKtPsi.*
import csense.kotlin.annotations.idea.analyzers.*
import csense.kotlin.extensions.collections.list.*
import csense.kotlin.extensions.collections.set.*
import csense.kotlin.specificExtensions.string.*
import org.jetbrains.kotlin.asJava.classes.*
import org.jetbrains.kotlin.asJava.elements.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.uast.*

object RequiresAnnotationAnalyzer : Analyzer<KtClassOrObject> {
    //for now do a bottom up ((slower?) than top down?)

    override fun analyze(item: KtClassOrObject): AnalyzerResult {
        if (item.isAbstract() || item.isInterfaceClass()) {
            return AnalyzerResult.empty
        }
        val project = item.project
        val extMgr = ExternalAnnotationsManager.getInstance(project)
        //Might be necessary if anything requires the annotation on sub classes (even though the parent has it).
        val annotations = item.resolveAllClassAnnotations(extMgr)
        val superAnnotations = item.computeSuperAnnotations(extMgr)

        val requiresAnnotation = annotations.filterRequiresAnnotation() + superAnnotations.filterRequiresAnnotation()
        if (requiresAnnotation.isEmpty()) {
            return AnalyzerResult.empty
        }
        val requiredTypesFqNames = requiresAnnotation.resolveRequiresAnnotationFqNames()
        val annotationsNames = annotations.map { it.qualifiedName ?: "" }.toSet()
        val diff = requiredTypesFqNames.symmetricDifference(annotationsNames)
        val missingRequiredAnnotations = diff.uniqueInFirst
        if (missingRequiredAnnotations.isEmpty()) {
            return AnalyzerResult.empty
        }
        return AnalyzerResult(
            missingRequiredAnnotations.map {
                AnalyzerError(
                    item,
                    "Not annotated with " +
                            it.modifications.wrapInQuotes() +
                            " which is required",
                    arrayOf()
                )
            }
        )
    }
}

fun List<UAnnotation>.resolveRequiresAnnotationFqNames(): Set<String> = mapNotNull {
    ((it.findAttributeValue("annotationClass")?.getExpressionType() as? PsiClassType)?.typeArguments()
        ?.firstOrNull() as? PsiClassReferenceType)?.reference?.qualifiedName
}.toSet()

fun List<UAnnotation>.filterRequiresAnnotation(): List<UAnnotation> = filter {
    it.qualifiedName in requiresAnnotationFqNames
}

val requiresAnnotationFqNames = setOf("csense.kotlin.annotations.inheritance.RequiresAnnotation")


//////------------------- base lib -----------------------//////////

fun KtClassOrObject.computeSuperAnnotations(extManager: ExternalAnnotationsManager): List<UAnnotation> {
    return this.toUElementOfType<UClass>()?.computeSuperAnnotationsForAll(extManager) ?: emptyList()
}

//only works for super classes
fun UClass.computeSuperAnnotations(extManager: ExternalAnnotationsManager): List<UAnnotation> {
    val annotations = mutableListOf<UAnnotation>()
    var currentSuper = javaPsi.superClass?.toUElementOfType<UClass>()
    while (currentSuper != null) {
        annotations += currentSuper.resolveAllClassAnnotations(extManager)
        currentSuper = currentSuper.javaPsi.superClass?.toUElementOfType()
    }
    return annotations
}

//works for super classes & interfaces
fun UClass.computeSuperAnnotationsForAll(extManager: ExternalAnnotationsManager): List<UAnnotation> {
    return this.uAnnotations + uastSuperTypes.map {
        val source = it.sourcePsi
        when (source) {
            is KtTypeReference -> source.resolve()?.toUElementOfType<UClass>()?.computeSuperAnnotationsForAll(extManager)
            is PsiReference -> source.resolve()?.toUElementOfType<UClass>()?.computeSuperAnnotationsForAll(extManager)
            else -> null
        } ?: it.uAnnotations
    }.flatten()
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


fun PsiElement.resolveAllMethodAnnotations(externalAnnotationsManager: ExternalAnnotationsManager? = null): List<UAnnotation> {
    val extManager = externalAnnotationsManager ?: ExternalAnnotationsManager.getInstance(project)
    val ownAnnotations = when (this) {
        is KtLightMethod -> annotations.mapNotNull { it.toUElementOfType<UAnnotation>() }
        is KtFunction -> annotationEntries.mapNotNull { it.toUElementOfType<UAnnotation>() }
        is PsiMethod -> annotations.mapNotNull { it.toUElementOfType<UAnnotation>() }
        else -> emptyList()
    }
    val external = if (this is PsiModifierListOwner) {
        extManager.findExternalAnnotations(this)?.mapNotNull {
            it.toUElementOfType<UAnnotation>()
        } ?: emptyList()
    } else {
        emptyList()
    }
    return ownAnnotations + external
}

fun PsiElement.resolveAllClassAnnotations(externalAnnotationsManager: ExternalAnnotationsManager? = null): List<UAnnotation> {
    val extManager = externalAnnotationsManager ?: ExternalAnnotationsManager.getInstance(project)
    val internal = when (this) {
        is KtLightClass -> annotations.mapNotNull { it.toUElementOfType<UAnnotation>() }
        is KtClass -> annotationEntries.mapNotNull { it.toUElementOfType<UAnnotation>() }
        is KtClassOrObject -> annotationEntries.mapNotNull { it.toUElementOfType<UAnnotation>() }
        is UAnnotated -> uAnnotations
        else -> emptyList()
    }
    val external = if (this is PsiModifierListOwner) {
        extManager.findExternalAnnotations(this)?.mapNotNull {
            it.toUElementOfType<UAnnotation>()
        } ?: emptyList()
    } else {
        emptyList()
    }
    return internal + external
}

fun PsiElement.resolveAllParameterAnnotations(externalAnnotationsManager: ExternalAnnotationsManager? = null): List<List<UAnnotation>> {
    val extManager = externalAnnotationsManager ?: ExternalAnnotationsManager.getInstance(project)
    return when (this) {
        is KtLightMethod -> parameterList.getAllAnnotations(extManager)
        is KtFunction -> valueParameters.getAllAnnotations(extManager)
        is PsiMethod -> parameterList.getAllAnnotations(extManager)
        else -> emptyList()
    }
}

fun List<KtParameter>.getAllAnnotations(
    extManager: ExternalAnnotationsManager
): List<List<UAnnotation>> = map {
    it.annotationEntries.toUAnnotation(extManager)
}

fun List<KtAnnotationEntry>.toUAnnotation(extManager: ExternalAnnotationsManager) = mapNotNull {
    it.toUElementOfType<UAnnotation>()
}

fun PsiParameterList.getAllAnnotations(extManager: ExternalAnnotationsManager): List<List<UAnnotation>> {
    val internal = parameters.map {
        it.annotations.mapNotNull { param -> param.toUElementOfType<UAnnotation>() }
    }
    val external = parameters.map {
        extManager.findExternalAnnotations(it)?.mapNotNull { param -> param.toUElementOfType<UAnnotation>() }
            ?: emptyList()
    }
    return internal.combine(external)
}