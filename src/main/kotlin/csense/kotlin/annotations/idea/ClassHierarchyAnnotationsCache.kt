package csense.kotlin.annotations.idea

import com.intellij.codeInsight.ExternalAnnotationsManager
import csense.kotlin.annotations.idea.psi.resolveAllClassAnnotations
import org.jetbrains.kotlin.idea.references.resolveMainReferenceToDescriptors
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElementOfType
import java.util.concurrent.ConcurrentHashMap

object ClassHierarchyAnnotationsCache {


    private val superTypeLookup: ConcurrentHashMap<UClass, List<UAnnotation>> =
            ConcurrentHashMap(500)

    /**
     * Tries to resolve the given class's parents annotations..
     * @return List<UAnnotation>
     */
    fun getClassHierarchyAnnotaions(
            clazz: KtClassOrObject?,
            extManager: ExternalAnnotationsManager): List<UAnnotation> {
        if (clazz == null) {
            return emptyList()
        }
        val superClz = clazz.superClass
        val superCache = superTypeLookup[superClz]
        val superAnnotations: List<UAnnotation> = superCache
                ?: clazz.computeSuperAnnotations(extManager).also {
                    println("computed super annotations anew")
                    superClz?.let { superClz -> superTypeLookup[superClz] = it }
                }

        val myAnnotations = clazz.resolveAllClassAnnotations(extManager)
        return myAnnotations + superAnnotations
    }
}

fun KtClassOrObject.computeSuperAnnotations(extManager: ExternalAnnotationsManager): List<UAnnotation> {
    val annotations = mutableListOf<UAnnotation>()
    var currentSuper = superClass
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
            return realClass?.toUElementOfType<UClass>()
        }
        return null
    }
