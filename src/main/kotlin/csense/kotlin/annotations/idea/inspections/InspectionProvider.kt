package csense.kotlin.annotations.idea.inspections

import com.intellij.codeInspection.InspectionToolProvider

class InspectionProvider : InspectionToolProvider {
    override fun getInspectionClasses(): Array<Class<*>> {
        return arrayOf(
//                QuickThreadingPropertyInspection::class.java,
                QuickThreadingCallInspection::class.java,
                QuickNumberRangeInspection::class.java,
                QuickNumberRangeTypeValueInspection::class.java,
                NoEscapeInspection::class.java,
                NoEscapeAssigmentInspection::class.java,
                ParameterLessConstructorInspection::class.java
        )
    }
}