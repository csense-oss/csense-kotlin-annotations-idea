package csense.kotlin.annotations.idea.externalAnnotations.ranges

import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.codeInsight.ExternalAnnotationsManager.CanceledConfigurationException
import com.intellij.codeInsight.ExternalAnnotationsManagerImpl
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.AddAnnotationPsiFix
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.JBUI
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.event.DocumentEvent


abstract class BaseExternalRangeAnnotation<Min, Max> : BaseIntentionAction() {

    /**
     * Eg by stating
     * owner is PsiField || owner is PsiParameter
     * means that it only works on fields and parameters.
     * @param element PsiElement
     * @return Boolean true if it may be annotated.
     */
    abstract fun canAnnotate(element: PsiElement): Boolean

    abstract val annotationName: String

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        val owner = findOwner(editor, file) ?: return false
        if (!ExternalAnnotationsManagerImpl.areExternalAnnotationsApplicable(owner)) {
            return false
        }
        return canAnnotate(owner)
    }


    fun findOwner(editor: Editor?, file: PsiFile?): PsiModifierListOwner? {
        //TODO find that method somewhere else... that is so .. !??
        val owner = AddAnnotationPsiFix.getContainer(file, editor?.caretModel?.offset ?: 0, true) ?: return null
        return (owner.originalElement as? PsiModifierListOwner) ?: owner
    }


    abstract fun getValueFrom(psiAnnotation: PsiAnnotation?): Pair<Min, Max>
    override fun startInWriteAction(): Boolean = false

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val owner: PsiModifierListOwner = findOwner(editor, file) ?: return
        val annotation = ExternalAnnotationsManager.getInstance(project)
                .findExternalAnnotation(owner, annotationName)
        val parsed = getValueFrom(annotation)
        val min = parsed.first.toString()
        val max = parsed.second.toString()
        val minText = JBTextField(min)
        val maxText = JBTextField(max)
        val builder: DialogBuilder = createDialog(project, minText, maxText) ?: return
        val validator: DocumentAdapter = object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                val error: String? = getErrorMessages(minText.text, maxText.text)
                if (error != null) {
                    builder.setOkActionEnabled(false)
                    builder.setErrorText(error, minText)
                    builder.setErrorText(error, maxText)
                } else {
                    builder.setOkActionEnabled(true)
                    builder.setErrorText(null)
                }
            }
        }
        minText.document.addDocumentListener(validator)
        maxText.document.addDocumentListener(validator)
        if (builder.showAndGet()) {
            updateRange(owner, minText.text, maxText.text);
        }
    }

    abstract fun validate(min: Min?, max: Max?): String?

    private fun getErrorMessages(min: String?, max: String?): String? {
        return validate(min?.toMin(), max?.toMax())
    }

    open fun createDialog(project: Project,
                          minText: JBTextField,
                          maxText: JBTextField): DialogBuilder? {
        val panel = JPanel(GridBagLayout())
        val c = GridBag()
                .setDefaultAnchor(GridBagConstraints.WEST)
                .setDefaultFill(GridBagConstraints.HORIZONTAL)
                .setDefaultInsets(JBUI.insets(2))
                .setDefaultWeightX(0, 1.0)
                .setDefaultWeightX(1, 3.0)
                .setDefaultWeightY(1.0)
        panel.add(Messages.configureMessagePaneUi(JTextPane(), promptText),
                c.nextLine().next().coverLine())
        val fromLabel = JLabel(fromText)
        fromLabel.setDisplayedMnemonic('f')
        fromLabel.labelFor = minText
        panel.add(fromLabel, c.nextLine().next())
        panel.add(minText, c.next())
        val toLabel = JLabel(toText)
        toLabel.setDisplayedMnemonic('t')
        toLabel.labelFor = maxText
        panel.add(toLabel, c.nextLine().next())
        panel.add(maxText, c.next())
        val builder = DialogBuilder(project).setNorthPanel(panel).title(title)
        builder.setPreferredFocusComponent(minText)
        return builder
    }

    abstract val title: String
    abstract val toText: String
    abstract val fromText: String
    abstract val promptText: String


    abstract fun String.toMin(): Min?

    abstract fun String.toMax(): Max?
    /**
     *
     * @param min Min
     * @param max Max
     * @return String eg "@IntLimit(from=0, to= 1)"
     */
    abstract fun createAnnotationCode(min: Min?, max: Max?): String

    private fun updateRange(owner: PsiModifierListOwner, min: String, max: String) {
        val minReal = min.trim().toMin()
        val maxReal = max.trim().toMax()
        val project = owner.project
        val manager = ExternalAnnotationsManager.getInstance(project)
        manager.deannotate(owner, annotationName)
        val mockAnno = JavaPsiFacade.getElementFactory(project)
                .createAnnotationFromText(createAnnotationCode(minReal, maxReal), null)
        try {
            manager.annotateExternally(owner, annotationName, owner.containingFile,
                    mockAnno.parameterList.attributes)
        } catch (ignored: CanceledConfigurationException) {

        }
        DaemonCodeAnalyzer.getInstance(project).restart()
    }
}