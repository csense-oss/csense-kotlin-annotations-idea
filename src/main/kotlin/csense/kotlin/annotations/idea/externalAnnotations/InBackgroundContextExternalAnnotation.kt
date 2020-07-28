package  csense.kotlin.annotations.idea.externalAnnotations

import com.intellij.psi.*
import csense.idea.base.externalAnnotations.*

class InBackgroundContextExternalAnnotation : ExternalMultiplePsiAnnotationProvider(name) {
    companion object {
        val name = "csense.kotlin.annotations.threading.InBackgroundContext"
    }
    
    override fun isUsableForType(owner: PsiModifierListOwner?): Boolean =
            owner is PsiClass
    
}
