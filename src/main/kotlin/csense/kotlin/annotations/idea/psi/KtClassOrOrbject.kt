package csense.kotlin.annotations.idea.psi

import com.intellij.codeInsight.ExternalAnnotationsManager
import org.jetbrains.kotlin.idea.references.resolveMainReferenceToDescriptors
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElementOfType


fun KtClassOrObject.computeSuperAnnotations(extManager: ExternalAnnotationsManager): List<UAnnotation> {
    return this.toUElementOfType<UClass>()?.computeSuperAnnotations(extManager) ?: emptyList()
}

fun UClass.computeSuperAnnotations(extManager: ExternalAnnotationsManager): List<UAnnotation> {
    val annotations = mutableListOf<UAnnotation>()
    var currentSuper = javaPsi.superClass?.toUElementOfType<UClass>()
    while (currentSuper != null) {
        annotations += currentSuper.resolveAllClassAnnotations(extManager)
        currentSuper = currentSuper.javaPsi.superClass?.toUElementOfType()
    }
    return annotations
}

val KtClassOrObject.superClass: UClass?
    get() {
        val superTypes = superTypeListEntries
        if (superTypes.isEmpty()) {
            return null
        }
        superTypes.forEach {
            val realClass = it.typeAsUserType?.referenceExpression?.resolveMainReferenceToDescriptors()
                    ?.firstOrNull()?.containingDeclaration?.findPsi()
            return realClass?.toUElementOfType()
        }
        return null
    }
