package csense.kotlin.annotations.idea.inspections

import com.intellij.codeInspection.InspectionToolProvider
import csense.kotlin.annotations.idea.inspections.inheritance.ParameterLessConstructorRequriedInspection
import csense.kotlin.annotations.idea.inspections.inheritance.SuperCallRequiredInspection
import csense.kotlin.annotations.idea.inspections.numbers.QuickNumberRangeInspection
import csense.kotlin.annotations.idea.inspections.numbers.QuickNumberRangeTypeValueInspection
import csense.kotlin.annotations.idea.inspections.properties.PropertyMustBeConstantInspection
import csense.kotlin.annotations.idea.inspections.sideeffect.NoEscapeAssigmentInspection
import csense.kotlin.annotations.idea.inspections.sideeffect.NoEscapeInspection
import csense.kotlin.annotations.idea.inspections.threading.QuickThreadingCallInspection
import csense.kotlin.annotations.idea.startup.*

class InspectionProvider : InspectionToolProvider {
    override fun getInspectionClasses(): Array<Class<*>> {
        StartupService.instance
        return arrayOf(
                QuickThreadingCallInspection::class.java,
                QuickNumberRangeInspection::class.java,
                QuickNumberRangeTypeValueInspection::class.java,
                NoEscapeInspection::class.java,
                NoEscapeAssigmentInspection::class.java,
                ParameterLessConstructorRequriedInspection::class.java,
                SuperCallRequiredInspection::class.java,
                PropertyMustBeConstantInspection::class.java
        )
    }
}