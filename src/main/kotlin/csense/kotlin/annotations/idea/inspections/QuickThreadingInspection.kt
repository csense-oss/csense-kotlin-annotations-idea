package csense.kotlin.annotations.idea.inspections

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import csense.kotlin.annotations.idea.Constants
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.references.resolveMainReferenceToDescriptors
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfType
import org.jetbrains.kotlin.resolve.calls.callUtil.getCalleeExpressionIfAny
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UResolvable
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.toUElementOfType

//purpose: to provide simple (non deep) inspection of threading issues (where we look at
// @AnyThread / @InUi threading
// we should also look into
// Android annotations & idea annotations to provide help in theses scenario's as well.

class QuickThreadingInspection : AbstractKotlinInspection() {
    //eg look for:
    //launch(Dispatchers.Main){
    //any thread access here
    // }
    //or opposite (where its not main but ui thread is called.

    override fun getDisplayName(): String {
        return "ThreadingInspector"
    }

    override fun getStaticDescription(): String? {
        return """
            
        """.trimIndent()
    }

    override fun getDescriptionFileName(): String? {
        return "more desc ? "
    }

    override fun getShortName(): String {
        return "ThreadingInspector"
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
                              isOnTheFly: Boolean): KtVisitorVoid {
        return namedFunctionVisitor {
            val assumeAllIsAny = false
            val thisThreading = it.ComputeThreading()

            val isThisBackground = assumeAllIsAny || thisThreading.isAnyThreaded
            //skip if no available data.
            if (!isThisBackground && !thisThreading.isUiThreaded) {
                //no knowledge of this method.. just skip.
                return@namedFunctionVisitor
            }
            //sanity check no one does both types at ones
            if (thisThreading.isAnyThreaded && thisThreading.isUiThreaded) {
                holder.registerProblem(it.nameIdentifier
                        ?: it, "This method is annotated both as any threaded and ui threaded. Annotate it according to what type its supposed to be.")
                return@namedFunctionVisitor
            }
            //now we are either of them, then collect and see if any children are the opposite type.

            //TODO when doing this, we are to "lookup" if the exp is inside of something that changes the "scope" of our threading.

            it.forEachDescendantOfType { exp: KtCallExpression ->


                val annot = exp.resolvePsi()?.toUElementOfType<UMethod>()?.annotations

                val threadTypes: Threading? = when (val resolvedChild = exp.resolvePsi()) {
                    is PsiMethod -> {
                        val extLookup = ExternalAnnotationsManager.getInstance(exp.project)
                        resolvedChild.ComputeThreading(extLookup)
                    }
                    is KtFunction -> resolvedChild.ComputeThreading()
                    else -> null
                }
                if (threadTypes != null) {
                    //todo go up the tree and see if we have any thread changing constructs; if so track it until our own function and see if the resulting one is ok.

                }
                if (threadTypes != null && !thisThreading.isValidFor(threadTypes)) {
                    holder.registerProblem(exp, "Invalid threading for this expression")
                }
            }

        }
    }
}

fun KtCallExpression.resolvePsi(): PsiElement? {
    return (this.toUElement() as? UResolvable)?.resolve()
            ?: getCalleeExpressionIfAny()?.resolveMainReferenceToDescriptors()?.firstOrNull()?.findPsi() ?: return null
}

fun KtFunction.ComputeThreading(): Threading = Threading(isAnyThreaded(), isUiThreaded())

fun PsiMethod.ComputeThreading(extLookup: ExternalAnnotationsManager): Threading = Threading(
        isAnyThreaded(extLookup),
        isUiThreaded(extLookup))

data class Threading(val isAnyThreaded: Boolean, val isUiThreaded: Boolean)

/**
 * If this is an invalid threading setting.
 * @receiver Threading
 * @return Boolean
 */
fun Threading.isInvalid(): Boolean = this.isUiThreaded && this.isAnyThreaded

fun Threading.isValidFor(other: Threading): Boolean {
    if (this.isAnyThreaded && other.isUiThreaded) {
        return false
    }
//    if (this.isUiThreaded)
    return true
    //may ui call any ? hmm

}

fun KtFunction.isUiThreaded(): Boolean = annotationEntries.any { it.shortName?.asString() in uiAnnotationsNames }

fun KtFunction.isAnyThreaded(): Boolean = annotationEntries.any { it.shortName?.asString() in otherThreadNames }

fun PsiMethod.isUiThreaded(extLookup: ExternalAnnotationsManager): Boolean = isInAny(extLookup, uiAnnotationsNames)

fun PsiMethod.isAnyThreaded(extLookup: ExternalAnnotationsManager): Boolean = isInAny(extLookup, otherThreadNames)

fun PsiMethod.isInAny(extLookup: ExternalAnnotationsManager, of: Set<String>): Boolean {
    val haveOwnAnnotations = annotations.any {
        it.qualifiedName in of
    }
    if (haveOwnAnnotations) {
        return true
    }
    val externalAnnotations = extLookup.findExternalAnnotations(this) ?: return false
    return externalAnnotations.any {
        of.contains(it.nameReferenceElement?.referenceName)
    }
}

val otherThreadNames = setOf(
        "AnyThread",//csense
        "AnyThread" //https://androidx.tech/artifacts/annotation/annotation/1.0.2-source/androidx/annotation/AnyThread.java.html
)

val uiAnnotationsNames = setOf(
        "InUi",  //csense
        "UiThread" //https://androidx.tech/artifacts/annotation/annotation/1.0.2-source/androidx/annotation/UiThread.java.html
)

fun KtNameReferenceExpression.findPsi(): PsiElement? {
    val referre = this.resolveMainReferenceToDescriptors().firstOrNull() ?: return null
    return referre.findPsi()
}