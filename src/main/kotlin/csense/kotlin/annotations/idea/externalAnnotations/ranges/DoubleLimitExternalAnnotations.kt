package csense.kotlin.annotations.idea.externalAnnotations.ranges

import com.intellij.psi.*
import csense.idea.base.externalAnnotations.*


class DoubleLimitExternalAnnotations : BaseExternalRangeAnnotation<Double, Double>() {
    override fun canAnnotate(element: PsiElement): Boolean {
        val isRightPsiType = element is PsiField || element is PsiParameter || element is PsiMethod
        if (!isRightPsiType) {
            return false
        }
        val type = (element as? PsiVariable)?.type ?: (element as? PsiMethod)?.returnType ?: return false
        return type == PsiType.DOUBLE
    }
    
    override fun getText(): String {
        return "Double Limit (range)"
    }
    
    override fun getFamilyName(): String {
        return "Edit double limit(Range)"
    }
    
    override val annotationName: String
        get() = "csense.kotlin.annotations.numbers.DoubleLimit"
    
    override fun String.toMin(): Double? = toDoubleOrNull()
    
    override fun String.toMax(): Double? = toDoubleOrNull()
    
    override fun createAnnotationCode(min: Double?, max: Double?): String = "@DoubleLimit(from=$min, to=$max)"
    
    override fun validate(min: Double?, max: Double?): String? {
        if (min == null || max == null) {
            return "Both values must be set"
        }
        if (max < min) {
            return "Min is larger than max"
        }
        return null
    }
    
    override fun getValueFrom(psiAnnotation: PsiAnnotation?): Pair<Double, Double> {
        return Pair(
                psiAnnotation?.findAttributeValue("from")?.text?.toMin() ?: 0.0,
                psiAnnotation?.findAttributeValue("to")?.text?.toMax() ?: 1.0)
    }
    
    override val title: String = "Edit double limit(range)"
    override val toText: String = "to value (inclusive)"
    override val fromText: String = "from value (inclusive)"
    override val promptText: String = "Please enter the values that are allowed for this double"
}
