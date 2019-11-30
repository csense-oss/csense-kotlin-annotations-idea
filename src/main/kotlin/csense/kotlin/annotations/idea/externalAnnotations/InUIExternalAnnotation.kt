package  csense.kotlin.annotations.idea.externalAnnotations

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner

class InUIExternalAnnotation : MultiplePsiAnnotationProvider(name) {
    companion object {
        val name = "csense.kotlin.annotations.threading.InUi"
    }

    override fun isUsableForType(owner: PsiModifierListOwner?): Boolean =
            owner is PsiMethod
}