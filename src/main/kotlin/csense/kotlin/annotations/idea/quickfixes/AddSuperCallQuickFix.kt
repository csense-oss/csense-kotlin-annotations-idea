package csense.kotlin.annotations.idea.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.util.IncorrectOperationException
import csense.idea.base.bll.kotlin.addFirstInScope
import csense.idea.base.bll.kotlin.convertToBlockFunction
import csense.idea.base.bll.psi.smartPsiElementPointer
import csense.kotlin.logger.logClassError
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory

class AddSuperCallQuickFix(fnc: KtNamedFunction) : LocalQuickFix {

    private val pointer = fnc.smartPsiElementPointer()

    override fun getFamilyName(): String = "Add super call"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val fnc = pointer.element ?: return
        val fncName = fnc.name ?: return
        val parameterNames = fnc.valueParameters.mapNotNull { it.name }.joinToString(",")
        val factory = KtPsiFactory(project)
        val superCallCodeText = "super.${fncName}($parameterNames)"
        //if its a body block, simple, just add it
        fnc.bodyBlockExpression?.let { bodyBlock ->
            val superCall = factory.createExpression(superCallCodeText)
            bodyBlock.addFirstInScope(superCall)
        }
        //if it is a body expression convert it to a body block and with the super call.
        fnc.bodyExpression?.let {
            val superFnc = fnc.convertToBlockFunction(factory) {
                "${superCallCodeText}\nreturn $it"
            }
            try {
                fnc.replace(superFnc)
            } catch (e: IncorrectOperationException) {
                logClassError("", e)
            }
        }
    }

    override fun startInWriteAction(): Boolean = true

}