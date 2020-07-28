package csense.kotlin.annotations.idea.analyzers.threading

import com.intellij.codeInsight.*
import com.intellij.psi.*
import csense.idea.base.cache.*
import csense.kotlin.annotations.idea.analyzers.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*


abstract class IterativeInspector {
    
    abstract fun isStateChange(psiElement: PsiElement): Boolean
    
    //update state
    abstract fun updateCurrentState(psiElement: PsiElement)
    
    //is this "ok" ?
    abstract fun inspectStatement(psiElement: PsiElement)
    
    
}

class ThreadingIterativeState(var currentScope: Threading? = null)

class ThreadingIterativeInspector(
        val extMan: ExternalAnnotationsManager
) : IterativeInspector() {
    
    private var stateType = ThreadingIterativeState(null)
    
    private var errors: MutableList<AnalyzerError> = mutableListOf()
    
    fun start(ourCallFunction: KtFunction): List<AnalyzerError> {
        stateType.currentScope = computeInitialState(ourCallFunction) ?: return errors
        if (isCallerDeclartionInvalid(ourCallFunction)) {
            errors.add(AnalyzerError(
                    ourCallFunction.nameIdentifier ?: ourCallFunction,
                    "This method is annotated with more than one threading type, please choose what this is...",
                    arrayOf()))
            return errors
        }
        //now we can "iterate" over the content.
        ourCallFunction.accept(object : KtVisitorVoid() {
            override fun visitKtElement(element: KtElement) {
                //compute here
                if (isStateChange(element)) {
            
                }
                if (shouldInspectStatement(element)) {
            
                }
                super.visitKtElement(element)
        
            }
        })
        return errors
    }
    
    fun isCallerDeclartionInvalid(ourCallFunction: KtFunction): Boolean {
        return ourCallFunction.computeIfThreadingIsInvalid(extMan)
    }
    
    fun computeInitialState(ourCallFunction: KtFunction): Threading? {
        return ourCallFunction.computeThreading(extMan)
                ?: computeParentThreading(ourCallFunction)
    }
    
    fun computeParentThreading(ourCallFunction: KtFunction): Threading? {
        return ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(
                ourCallFunction.containingClassOrObject,
                extMan).computeThreadingContext()
    }
    
    override fun isStateChange(psiElement: PsiElement): Boolean {
        //if is known construct or if it is marked.
        return when (psiElement) {
            is KtCallExpression -> psiElement.isInbuiltConstruct() //find out if its annotated or if the lambda is
            is KtLambdaArgument -> true//hmm
            else -> false
        }
    }
    
    override fun updateCurrentState(psiElement: PsiElement) {
        TODO("Not yet implemented")
    }
    
    /**
     * We are only interested in reporting for these types
     * @param psiElement PsiElement
     * @return Boolean
     */
    fun shouldInspectStatement(psiElement: PsiElement): Boolean {
        return when (psiElement) {
            is KtCallExpression -> true
            is KtNameReferenceExpression -> true
            else -> false
        }
    }
    
    override fun inspectStatement(psiElement: PsiElement) {
        val expState = psiElement.computeThreading(extMan)
        if (expState != null && stateType.currentScope?.isInValidFor(expState) == true) {
            //errors.add()
        }
    }
}


open class KtTreeVisitorVoidBfsVoid : KtVisitorVoid() {
    
    private val childrenNeedsToBeVisited: MutableList<PsiElement> = mutableListOf()
    
    fun visit(psiElement: PsiElement){
        childrenNeedsToBeVisited += psiElement.children
        var currentElement = psiElement
        while(childrenNeedsToBeVisited.isNotEmpty()){
            childrenNeedsToBeVisited.removeAt(0).acceptChildren(this)
        }
        
    }
    
    override fun visitElement(element: PsiElement) {
    
    }
}