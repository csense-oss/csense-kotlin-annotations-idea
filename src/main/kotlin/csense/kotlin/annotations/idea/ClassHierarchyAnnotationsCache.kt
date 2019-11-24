package csense.kotlin.annotations.idea

import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.psi.PsiClass
import csense.kotlin.annotations.idea.psi.computeSuperAnnotations
import csense.kotlin.annotations.idea.psi.resolveAllClassAnnotations
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
            clazz: UClass?,
            extManager: ExternalAnnotationsManager): List<UAnnotation> {
        if (clazz == null) {
            return emptyList()
        }
        val superClz = clazz.javaPsi.superClass?.toUElementOfType<UClass>()
                ?: return clazz.resolveAllClassAnnotations(extManager)
        val superCache = superTypeLookup[superClz]
        val superAnnotations: List<UAnnotation> = superCache
                ?: clazz.computeSuperAnnotations(extManager).also {
                    superTypeLookup[superClz] = it
                }

        val myAnnotations = clazz.resolveAllClassAnnotations(extManager)
        return myAnnotations + superAnnotations
    }
    fun getClassHierarchyAnnotaions(
            clazz: KtClassOrObject?,
            extManager: ExternalAnnotationsManager): List<UAnnotation> {
        return getClassHierarchyAnnotaions(clazz?.toUElementOfType<UClass>(),extManager)
    }

    fun getClassHierarchyAnnotaions(
            clazz: PsiClass?,
            extManager: ExternalAnnotationsManager): List<UAnnotation> {
        return getClassHierarchyAnnotaions(clazz?.toUElementOfType<UClass>(),extManager)
    }
}