package csense.kotlin.annotations.idea.externalAnnotations

import com.intellij.codeInsight.externalAnnotation.AnnotationProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner

abstract class SimplePsiClassAnnotationsProvider(private val name: String) : AnnotationProvider {
    override fun getName(project: Project?): String = name

    override fun isAvailable(owner: PsiModifierListOwner?): Boolean =
            owner is PsiClass && !owner.hasAnnotation(name)
}

