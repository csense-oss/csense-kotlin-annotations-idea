package csense.kotlin.annotations.idea.bll

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.psi.KtAnnotation
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFunction

/**
 * Shared concept of a MPP capable annotation (UAST does not work for MPP projects.)
 */
data class MppAnnotation(val qualifiedName: String)

fun PsiAnnotation.toMppAnnotation(): MppAnnotation? {
    val qualifiedName = qualifiedName ?: return null
    return MppAnnotation(qualifiedName)
}

fun KtAnnotation.toMppAnnotation(): MppAnnotation? {
    val asLight = toLightAnnotation()
    val qualifiedName = asLight?.qualifiedName ?: return null
    return MppAnnotation(qualifiedName)
}

fun KtAnnotationEntry.toMppAnnotation(): MppAnnotation? {
    //TODO make me..
    val asLight = toLightAnnotation()

    val qualifiedName = asLight?.qualifiedName ?: return null
    return MppAnnotation(qualifiedName)
}

fun PsiElement.getItemMppAnnotations(): List<MppAnnotation> = when (this) {
    is KtLightMethod -> annotations.toMppAnnotations()
    is KtFunction -> annotationEntries.toMppAnnotations()
    is PsiMethod -> annotations.toMppAnnotations()
    else -> emptyList()
}

fun List<KtAnnotationEntry>.toMppAnnotations():List<MppAnnotation> = mapNotNull { it.toMppAnnotation() }
fun Array<PsiAnnotation>.toMppAnnotations(): List<MppAnnotation> = mapNotNull { it.toMppAnnotation() }