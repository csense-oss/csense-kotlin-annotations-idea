package csense.kotlin.annotations.idea.inspections

import com.intellij.codeInspection.InspectionToolProvider

class InspectionProvider : InspectionToolProvider {
    override fun getInspectionClasses(): Array<Class<*>> {
        return arrayOf(
                QuickThreadingInspection::class.java,
                QuickNumberRangeInspection::class.java,
                QuickNumberRangeTypeValueInspection::class.java
        )
    }
}