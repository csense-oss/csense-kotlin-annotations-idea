package csense.kotlin.annotations.idea.externalAnnotations.ranges

import com.intellij.psi.*
import csense.idea.base.externalAnnotations.*


class FloatLimitExternalAnnotations : BaseExternalRangeAnnotation<Float, Float>() {
    override fun canAnnotate(element: PsiElement): Boolean {
        val isRightPsiType = element is PsiField || element is PsiParameter || element is PsiMethod
        if (!isRightPsiType) {
            return false
        }
        val type = (element as? PsiVariable)?.type ?: (element as? PsiMethod)?.returnType ?: return false
        return type == PsiType.FLOAT
    }
    
    override fun getText(): String {
        return "Float Limit (range)"
    }
    
    override fun getFamilyName(): String {
        return "Edit float limit(Range)"
    }
    
    override val annotationName: String
        get() = "csense.kotlin.annotations.numbers.FloatLimit"
    
    override fun String.toMin(): Float? = toFloatOrNull()
    
    override fun String.toMax(): Float? = toFloatOrNull()
    
    override fun createAnnotationCode(min: Float?, max: Float?): String = "@FloatLimit(from=$min, to=$max)"
    
    override fun validate(min: Float?, max: Float?): String? {
        if (min == null || max == null) {
            return "Both values must be set"
        }
        if (max < min) {
            return "Min is larger than max"
        }
        return null
    }
    
    override fun getValueFrom(psiAnnotation: PsiAnnotation?): Pair<Float, Float> {
        return Pair(
                psiAnnotation?.findAttributeValue("from")?.text?.toMin() ?: 0f,
                psiAnnotation?.findAttributeValue("to")?.text?.toMax() ?: 1f)
    }
    
    override val title: String = "Edit float limit(range)"
    override val toText: String = "to value (inclusive)"
    override val fromText: String = "from value (inclusive)"
    override val promptText: String = "Please enter the values that are allowed for this float"
}
