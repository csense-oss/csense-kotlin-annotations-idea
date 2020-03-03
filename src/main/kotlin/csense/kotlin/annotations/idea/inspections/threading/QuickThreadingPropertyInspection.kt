package csense.kotlin.annotations.idea.inspections.threading//package csense.kotlin.annotations.idea.inspections
//
//import com.intellij.codeHighlighting.HighlightDisplayLevel
//import com.intellij.codeInsight.ExternalAnnotationsManager
//import com.intellij.codeInspection.ProblemsHolder
//import com.intellij.psi.PsiElement
//import com.intellij.psi.PsiMethod
//import csense.kotlin.Function0
//import csense.kotlin.annotations.idea.ClassHierarchyAnnotationsCache
//import csense.kotlin.annotations.idea.Constants
//import csense.kotlin.annotations.idea.bll.MppAnnotation
//import csense.kotlin.annotations.idea.bll.getKotlinFqNameString
//import csense.kotlin.annotations.idea.psi.resolveAllMethodAnnotationMppAnnotation
//import csense.kotlin.extensions.map
//import org.jetbrains.kotlin.asJava.elements.KtLightMethod
//import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
//import org.jetbrains.kotlin.idea.references.resolveMainReferenceToDescriptors
//import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
//import org.jetbrains.kotlin.psi.*
//import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
//import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfType
//import org.jetbrains.kotlin.resolve.calls.callUtil.getCalleeExpressionIfAny
//import org.jetbrains.uast.*
//
//class QuickThreadingPropertyInspection : AbstractKotlinInspection() {
//
//    override fun getDisplayName(): String {
//        return "ThreadingPropertyInspector"
//    }
//
//    override fun getStaticDescription(): String? {
//        return """
//
//        """.trimIndent()
//    }
//
//    override fun getDescriptionFileName(): String? {
//        return "more desc ? "
//    }
//
//    override fun getShortName(): String {
//        return "ThreadingPropertyInspector"
//    }
//
//    override fun getGroupDisplayName(): String {
//        return Constants.InspectionGroupName
//    }
//
//    override fun getDefaultLevel(): HighlightDisplayLevel {
//        return HighlightDisplayLevel.ERROR
//    }
//
//    override fun isEnabledByDefault(): Boolean {
//        return true
//    }
//
//    override fun buildVisitor(holder: ProblemsHolder,
//                              isOnTheFly: Boolean): KtVisitorVoid {
//        return propertyAccessorVisitor {
//            val extMan = ExternalAnnotationsManager.getInstance(it.project)
//            val prop = it.property
//
//            val threadingOfProp = when {
//                it.isGetter -> prop.getter?.computeThreading(extMan)
//                it.isSetter -> prop.setter?.computeThreading(extMan)
//                else -> null
//            } ?: prop.computeThreading(extMan) ?: return@propertyAccessorVisitor // prop not annotated
//            val currentMethod = it.findParentOfType<KtFunction>()
//            val methodThreading = currentMethod?.computeThreading(extMan)
//            if (methodThreading != null && methodThreading.isInValidFor(threadingOfProp)) {
//                val error = methodThreading.computeErrorMessageTo(threadingOfProp)
//                holder.registerProblem(it, error)
//                return@propertyAccessorVisitor
//            }
//            val classHierarchyAnnotations = ClassHierarchyAnnotationsCache.getClassHierarchyAnnotations(it.containingClassOrObject, extMan)
//            val classContextThreading = classHierarchyAnnotations.computeAnnotationContext()
//            if (classContextThreading != null && classContextThreading.isInValidFor(threadingOfProp)) {
//                val error = classContextThreading.computeErrorMessageTo(threadingOfProp)
//                holder.registerProblem(it, error)
//                return@propertyAccessorVisitor
//
//            }
//        }
//    }
//}
