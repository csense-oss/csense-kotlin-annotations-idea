package csense.kotlin.annotations.idea.externalAnnotations

import com.intellij.psi.*
import csense.idea.base.externalAnnotations.*

class NoEscapeExternalAnnotations : ExternalMultiplePsiAnnotationProvider(name) {
    
    companion object {
        val name: String = "csense.kotlin.annotations.sideEffect.NoEscape"
    }
    
    override fun isUsableForType(owner: PsiModifierListOwner?): Boolean =
            owner is PsiField || owner is PsiMethod || owner is PsiTypeParameter ||
                    owner is PsiParameter //TODO not sure.
}