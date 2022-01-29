package csense.kotlin.annotations.idea.threading

import csense.idea.kotlin.test.*
import csense.kotlin.annotations.idea.inspections.threading.*
import org.junit.*

class QuickThreadingCallTests: KotlinLightCodeInsightFixtureTestCaseJunit4() {
    override fun getTestDataPath(): String {
        return "src/test/testData/Threading/"
    }

    @Before
    fun setup() {
        myFixture.enableInspections(QuickThreadingCallInspection())
        myFixture.allowTreeAccessForAllFiles()
    }

    @Test
    fun uiAndNone(){
        myFixture.testHighlighting("UIAndNone.kt")
    }

    @Test
    fun backgroundAndNone(){
        myFixture.testHighlighting("BackgroundAndNone.kt")
    }

    @Test
    fun uiAndBackground(){
        myFixture.testHighlighting("UIAndBackground.kt")
    }

    @Test
    fun backgroundAndAny(){
        myFixture.testHighlighting("BackgroundAndAny.kt")
    }

    @Test
    fun uiAndAny(){
        myFixture.testHighlighting("UIAndAny.kt")
    }
}