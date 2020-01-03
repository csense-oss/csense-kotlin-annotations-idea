package csense.kotlin.annotations.idea.externalAnnotations

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifierListOwner
import csense.idea.base.externalAnnotations.ExternalMultiplePsiAnnotationProvider

class InUiContextExternalAnnotation : ExternalMultiplePsiAnnotationProvider(name) {
    companion object {
        val name = "csense.kotlin.annotations.threading.InUiContext"
    }

    override fun isUsableForType(owner: PsiModifierListOwner?): Boolean =
        owner is PsiClass

}