package csense.kotlin.annotations.idea.numbers.call

import csense.idea.kotlin.test.*
import csense.kotlin.annotations.idea.inspections.numbers.*
import org.junit.*

class CsenseRangeCallTest : KotlinLightCodeInsightFixtureTestCaseJunit4() {
    override fun getTestDataPath(): String {
        return "src/test/testData/NumberRange/call/"
    }

    @Before
    fun setup() {
        myFixture.enableInspections(QuickNumberRangeCallInspection())
        myFixture.allowTreeAccessForAllFiles()
    }

    @Test
    fun csenseByteLimit() {
        myFixture.testHighlighting("csenseByteLimit.kt")
    }

    @Test
    fun csenseIntLimit() {
        myFixture.testHighlighting("csenseIntLimit.kt")
    }

    @Test
    fun csenseDoubleLimit() {
        myFixture.testHighlighting("csenseDoubleLimit.kt")
    }

    @Test
    fun csenseFloatLimit() {
        myFixture.testHighlighting("csenseFloatLimit.kt")
    }

    @Test
    fun csenseLongLimit() {
        myFixture.testHighlighting("csenseLongLimit.kt")
    }

    @Test
    fun csenseNumberLimit() {
        myFixture.testHighlighting("csenseNumberLimit.kt")
    }

    @Test
    fun csenseShortLimit() {
        myFixture.testHighlighting("csenseShortLimit.kt")
    }

}