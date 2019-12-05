package csense.kotlin.annotations.idea.psi

import com.intellij.codeInsight.ExternalAnnotationsManager
import csense.kotlin.annotations.idea.bll.MppAnnotation
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElementOfType

fun UClass.computeSuperAnnotations(extManager: ExternalAnnotationsManager): List<UAnnotation> {
    val annotations = mutableListOf<UAnnotation>()
    var currentSuper = javaPsi.superClass?.toUElementOfType<UClass>()
    while (currentSuper != null) {
        annotations += currentSuper.resolveAllClassAnnotations(extManager)
        currentSuper = currentSuper.javaPsi.superClass?.toUElementOfType()
    }
    return annotations
}


fun UClass.computeSuperMppAnnotations(extManager: ExternalAnnotationsManager): List<MppAnnotation> {
    val annotations = mutableListOf<MppAnnotation>()
    var currentSuper = javaPsi.superClass
    while (currentSuper != null) {
        annotations += currentSuper.resolveAllClassMppAnnotation(extManager)
        currentSuper = currentSuper.superClass
    }
    return annotations
}
