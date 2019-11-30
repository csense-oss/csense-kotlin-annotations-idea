package  csense.kotlin.annotations.idea.externalAnnotations

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifierListOwner

class InBackgroundContextExternalAnnotation : MultiplePsiAnnotationProvider(name) {
    companion object {
        val name = "csense.kotlin.annotations.threading.InBackgroundContext"
    }

    override fun isUsableForType(owner: PsiModifierListOwner?): Boolean =
            owner is PsiClass

}
