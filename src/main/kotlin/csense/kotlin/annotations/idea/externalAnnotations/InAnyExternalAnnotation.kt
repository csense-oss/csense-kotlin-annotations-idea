package  csense.kotlin.annotations.idea.externalAnnotations

import com.intellij.psi.*
import csense.idea.base.externalAnnotations.*

class InAnyExternalAnnotation : ExternalMultiplePsiAnnotationProvider(name) {
    companion object {
        val name = "csense.kotlin.annotations.threading.InAny"
    }
    
    override fun isUsableForType(owner: PsiModifierListOwner?): Boolean =
            owner is PsiMethod
}
