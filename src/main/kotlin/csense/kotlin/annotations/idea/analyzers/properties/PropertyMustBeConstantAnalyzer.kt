package csense.kotlin.annotations.idea.analyzers.properties

import csense.idea.base.bll.kotlin.*
import csense.idea.base.mpp.*
import csense.kotlin.annotations.idea.analyzers.*
import csense.kotlin.annotations.idea.inspections.properties.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

object PropertyMustBeConstantAnalyzer : Analyzer<KtProperty> {
    override fun analyze(item: KtProperty): AnalyzerResult {
        val errors = mutableListOf<AnalyzerError>()
        
        if (!item.isOverriding() && !item.hasPropertyMustBeContant()) {
            //only consider overwritten or those directly annotated.
            return AnalyzerResult(errors)
        }
        //dont look at "valid" constant properties.
        val getter = item.getter
        if (item.initializer != null && getter == null && item.setter == null && !item.isVar) {
            //nothing to validate then.
            return AnalyzerResult(errors)
        }
        //if the getter is sufficient simple its properly constant and we "ignore" it.
        if (getter != null && item.setter == null && !item.isVar && getter.isProperlyConstant()) {
            return AnalyzerResult(errors)
        }
        
        if (!item.hasPropertyMustBeContant() && !item.isOverridingMustBeConstantProperty()) {
            AnalyzerResult(errors)
        }
        //we have a "non trivial " getter (or alike) and or its a setter, and its must be a constant.
        if (item.setter != null || item.isVar) {
            
            errors.add(AnalyzerError(
                    item,
                    "Property must be constant, thus you are violating the contract. a setter / var is not constant",
                    arrayOf()))
        } else if (item.getter != null) {
            errors.add(AnalyzerError(
                    item,
                    "Getter is properly not constant.",
                    arrayOf()))
        }
        
        
        return AnalyzerResult(errors)
    }
}

fun KtPropertyAccessor.isProperlyConstant(): Boolean {
//properly constant things; a) field accesses; b very little code.
    val code = bodyExpression ?: bodyBlockExpression ?: return false
    return !code.anyDescendantOfType<KtCallExpression>()
}

fun KtProperty.hasPropertyMustBeContant(): Boolean = annotationEntries
        .toMppAnnotations()
        .containsPropertyMustBeConstant()


fun KtProperty.isOverridingMustBeConstantProperty(): Boolean {
    return findOverridingImpl()?.hasPropertyMustBeContant()
            ?: return false
}

fun List<MppAnnotation>.containsPropertyMustBeConstant(): Boolean = any {
    it.qualifiedName == propertyMustBeConstantName
}

private const val propertyMustBeConstantName = "csense.kotlin.annotations.properties.PropertyMustBeConstant"
