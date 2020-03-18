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
import csense.kotlin.annotations.idea.analyzers.*
import csense.kotlin.annotations.idea.analyzers.properties.*
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
    ): KtVisitorVoid = propertyVisitor { property ->
        val result = PropertyMustBeConstantAnalyzer.analyze(property)
        result.errors.forEach { holder.registerProblem(it) }
    }
}
