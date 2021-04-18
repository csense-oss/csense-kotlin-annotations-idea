package csense.kotlin.annotations.idea.inspections.threading

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.openapi.diagnostic.Logger
import csense.kotlin.annotations.idea.*
import csense.kotlin.annotations.idea.analyzers.*
import csense.kotlin.annotations.idea.analyzers.threading.*
import csense.kotlin.extensions.*
import csense.kotlin.logger.L
import csense.kotlin.logger.usePrintAsLoggers
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*

//purpose: to provide simple (non deep) inspection of threading issues (where we look at
// @AnyThread / @InUi threading
// we should also look into
// Android annotations & idea annotations to provide help in theses scenario's as well.
// well the "AnyThread" is any method that is not annotated.
// it should be "worker" / "background" ish something.
// the same for "IO" (which is also bad to do on a UI thread).


class QuickThreadingCallInspection : AbstractKotlinInspection() {
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
            This inspection provides highlighting for mixing of ui / background designed work.
            In most ui frameworks you are only allowed to update the ui from a specific thread, thus this can help with that.
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

    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean
    ): KtVisitorVoid {
        return namedFunctionVisitor { ourCallFunction: KtNamedFunction ->
            val result = logMeasureTimeInMillis {
                ThreadingCallInspectionAnalyzer.analyze(ourCallFunction)
            }
            result.errors.forEach {
                holder.registerProblem(it)
            }
        }
    }
//
//    companion object {
//        init {
//            L.usePrintAsLoggers()
////            L.debugLoggers.add { tag: String, message: String, throwable: Throwable? ->
////                Logger.getInstance(tag).debug(message, throwable)
////            }
//            L.isLoggingAllowed(true)
//        }
//    }
}
