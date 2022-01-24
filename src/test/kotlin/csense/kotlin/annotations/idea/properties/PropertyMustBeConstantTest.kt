package csense.kotlin.annotations.idea.properties

import csense.idea.kotlin.test.*
import csense.kotlin.annotations.idea.inspections.numbers.*
import csense.kotlin.annotations.idea.inspections.properties.*
import csense.kotlin.annotations.properties.*
import org.junit.*

class PropertyMustBeConstantTest : KotlinLightCodeInsightFixtureTestCaseJunit4() {
    override fun getTestDataPath(): String {
        return "src/test/testData/PropertyMustBeConstant/"
    }

    @Before
    fun setup() {
        myFixture.enableInspections(PropertyMustBeConstantInspection())
        myFixture.allowTreeAccessForAllFiles()
    }

    @Test
    fun nonConstant() {
        myFixture.testHighlighting("NonConstantProperty.kt")
    }

    @Test
    fun constant() {
        myFixture.testHighlighting("ConstantProperty.kt")
    }
}