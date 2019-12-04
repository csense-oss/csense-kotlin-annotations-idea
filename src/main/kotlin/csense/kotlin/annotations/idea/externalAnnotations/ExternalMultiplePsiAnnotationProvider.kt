package csense.kotlin.annotations.idea.externalAnnotations

import com.intellij.codeInsight.externalAnnotation.AnnotationProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiModifierListOwner

abstract class ExternalMultiplePsiAnnotationProvider(private val name: String) : AnnotationProvider {
    override fun getName(project: Project?): String = name

    abstract fun isUsableForType(owner: PsiModifierListOwner?): Boolean

    override fun isAvailable(owner: PsiModifierListOwner?): Boolean =
            owner?.hasAnnotation(name) != true && isUsableForType(owner)

}

