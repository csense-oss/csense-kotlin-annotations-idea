package csense.kotlin.annotations.idea.inspections

import com.intellij.codeInspection.*
import csense.kotlin.annotations.idea.inspections.inheritance.*
import csense.kotlin.annotations.idea.inspections.numbers.*
import csense.kotlin.annotations.idea.inspections.properties.*
import csense.kotlin.annotations.idea.inspections.sideeffect.*
import csense.kotlin.annotations.idea.inspections.threading.*
import csense.kotlin.annotations.idea.startup.*

class InspectionProvider : InspectionToolProvider {

    override fun getInspectionClasses(): Array<Class<out LocalInspectionTool>> {
        StartupService.instance
        return arrayOf(
                QuickThreadingCallInspection::class.java,
                QuickNumberRangeCallInspection::class.java,
                QuickNumberRangeTypeValueInspection::class.java,
                QuickNumberRangeParameterInspection::class.java,
                QuickNumberRangeVariableInspection::class.java,
                NoEscapeInspection::class.java,
                NoEscapeAssignmentInspection::class.java,
                ParameterLessConstructorRequriedInspection::class.java,
                SuperCallRequiredInspection::class.java,
                PropertyMustBeConstantInspection::class.java, 
                RequiresAnnotationInspection::class.java
        )
    }
}