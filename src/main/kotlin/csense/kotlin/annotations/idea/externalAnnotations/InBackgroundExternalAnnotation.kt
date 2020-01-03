package  csense.kotlin.annotations.idea.externalAnnotations

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner
import csense.idea.base.externalAnnotations.ExternalMultiplePsiAnnotationProvider

class InBackgroundExternalAnnotation : ExternalMultiplePsiAnnotationProvider(name) {
    companion object {
        val name = "csense.kotlin.annotations.threading.InBackground"
    }

    override fun isUsableForType(owner: PsiModifierListOwner?): Boolean =
            owner is PsiMethod
}
