package  csense.kotlin.annotations.idea.externalAnnotations

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner

class InAnyExternalAnnotation : MultiplePsiAnnotationProvider(name) {
    companion object {
        val name = "csense.kotlin.annotations.threading.InAny"
    }

    override fun isUsableForType(owner: PsiModifierListOwner?): Boolean =
            owner is PsiMethod
}
