package  csense.kotlin.annotations.idea.externalAnnotations

import com.intellij.psi.*
import csense.idea.base.externalAnnotations.*

class InBackgroundExternalAnnotation : ExternalMultiplePsiAnnotationProvider(name) {
    companion object {
        val name = "csense.kotlin.annotations.threading.InBackground"
    }
    
    override fun isUsableForType(owner: PsiModifierListOwner?): Boolean =
            owner is PsiMethod
}
