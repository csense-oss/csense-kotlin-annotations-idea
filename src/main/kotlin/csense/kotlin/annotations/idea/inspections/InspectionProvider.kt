package csense.kotlin.annotations.idea.inspections

import com.intellij.codeInspection.*
import csense.kotlin.annotations.idea.inspections.inheritance.*
import csense.kotlin.annotations.idea.inspections.numbers.*
import csense.kotlin.annotations.idea.inspections.properties.*
import csense.kotlin.annotations.idea.inspections.sideeffect.*
import csense.kotlin.annotations.idea.inspections.threading.*
import csense.kotlin.annotations.idea.startup.*

class InspectionProvider : InspectionToolProvider {
    override fun getInspectionClasses(): Array<Class<*>> {
        StartupService.instance
        return arrayOf(
                QuickThreadingCallInspection::class.java,
                QuickNumberRangeInspection::class.java,
                QuickNumberRangeTypeValueInspection::class.java,
                NoEscapeInspection::class.java,
                NoEscapeAssignmentInspection::class.java,
                ParameterLessConstructorRequriedInspection::class.java,
                SuperCallRequiredInspection::class.java,
                PropertyMustBeConstantInspection::class.java
        )
    }
}