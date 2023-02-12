package csense.kotlin.annotations.idea.inspections

import com.intellij.codeInspection.*
import csense.kotlin.annotations.idea.inspections.inheritance.*
import csense.kotlin.annotations.idea.inspections.numbers.*
import csense.kotlin.annotations.idea.inspections.properties.*
import csense.kotlin.annotations.idea.inspections.sideeffect.*
import csense.kotlin.annotations.idea.inspections.threading.*

class InspectionProvider : InspectionToolProvider {

    override fun getInspectionClasses(): Array<Class<out LocalInspectionTool>> =
        quickNumberInspections +
                arrayOf(
                    QuickThreadingCallInspection::class.java,

                    NoEscapeInspection::class.java,
                    NoEscapeAssignmentInspection::class.java,
//                    ParameterLessConstructorRequriedInspection::class.java,
//                    SuperCallRequiredInspection::class.java,
                    PropertyMustBeConstantInspection::class.java,
                    RequiresAnnotationInspection::class.java
                )

    private val quickNumberInspections: Array<Class<out LocalInspectionTool>> = arrayOf(
        QuickNumberRangeParameterCallInspection::class.java,
        QuickNumberRangeTypeValueInspection::class.java,
        QuickNumberRangeTypeValueConstructorInspection::class.java,
        QuickNumberRangeDefaultParameterInspection::class.java,
        QuickNumberRangeVariableDeclarationInspection::class.java,
        QuickNumberRangeVariableAssignmentInspection::class.java,
        QuickNumberRangeFunctionReturnInspection::class.java
    )
}