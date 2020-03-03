package csense.kotlin.annotations.idea.inspections.properties

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import csense.idea.base.UastKtPsi.resolvePsi
import csense.idea.base.bll.kotlin.findOverridingImpl
import csense.idea.base.bll.kotlin.isOverriding
import csense.idea.base.bll.kotlin.superClass
import csense.idea.base.bll.psi.getKotlinFqName
import csense.idea.base.mpp.MppAnnotation
import csense.idea.base.mpp.toMppAnnotation
import csense.idea.base.mpp.toMppAnnotations
import csense.kotlin.annotations.idea.Constants
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.findPropertyByName

class PropertyMustBeConstantInspection : AbstractKotlinInspection() {
    override fun getDisplayName(): String {
        return "PropertyMustBeConstantInspection"
    }

    override fun getStaticDescription(): String? {
        return """
            
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String? {
        return "more desc ? "
    }

    override fun getShortName(): String {
        return "PropertyMustBeConstantInspection"
    }

    override fun getGroupDisplayName(): String {
        return Constants.InspectionGroupName
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.ERROR
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean
    ): KtVisitorVoid = propertyVisitor {
        if (!it.isOverriding() && !it.hasPropertyMustBeContant()) {
            //only consider overwritten or those directly annotated.
            return@propertyVisitor
        }
        //dont look at "valid" constant properties.
        val getter = it.getter
        if (it.initializer != null && getter == null && it.setter == null && !it.isVar) {
            //nothing to validate then.
            return@propertyVisitor
        }
        //if the getter is sufficient simple its properly constant and we "ignore" it.
        if (getter != null && it.setter == null && !it.isVar && getter.isProperlyConstant()) {
            return@propertyVisitor
        }

        if (!it.hasPropertyMustBeContant() && !it.isOverridingMustBeConstantProperty()) {
            return@propertyVisitor
        }
        //we have a "non trivial " getter (or alike) and or its a setter, and its must be a constant.
        if (it.setter != null || it.isVar) {
            holder.registerProblem(it, "Property must be constant, thus you are violating the contract. a setter / var is not constant")
        } else if (it.getter != null) {
            holder.registerProblem(it, "Getter is properly not constant.")
        }

    }

    val x = "hej med dig"
    val xx: String
        get() = "hej med dig".toLowerCase()
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
