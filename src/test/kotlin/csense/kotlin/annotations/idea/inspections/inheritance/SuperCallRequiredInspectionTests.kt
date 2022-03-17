package csense.kotlin.annotations.idea.inspections.inheritance

import csense.idea.kotlin.test.*
import csense.kotlin.annotations.idea.inspections.inheritance.*
import org.junit.*

class SuperCallRequiredInspectionTests : KotlinLightCodeInsightFixtureTestCaseJunit4() {
    override fun getTestDataPath(): String {
        return "src/test/testData/SuperCallRequired/"
    }


    @Before
    fun setup() {
        myFixture.enableInspections(SuperCallRequiredInspection())
        myFixture.allowTreeAccessForAllFiles()
    }


    @Test
    fun superCalled() {
        myFixture.testHighlighting("SuperCalled.kt")
    }

    @Test
    fun superCallMissing() {
        myFixture.testHighlighting("SuperMissing.kt")
    }

    @Test
    fun worksOnAndroidX() {
        myFixture.testHighlighting("SuperNotCalledAndroidX.kt")
    }

    @Test
    fun worksOnAndroidSupport() {
        myFixture.testHighlighting("SuperNotCalledAndroidSupport.kt")
    }

}