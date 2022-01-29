package csense.kotlin.annotations.idea.sideeffect

import csense.idea.kotlin.test.*
import csense.kotlin.annotations.idea.inspections.sideeffect.*
import org.junit.*

class NoEscapeTests : KotlinLightCodeInsightFixtureTestCaseJunit4() {
    override fun getTestDataPath(): String {
        return "src/test/testData/NoEscape/"
    }

    @Before
    fun setup() {
        myFixture.enableInspections(NoEscapeAssignmentInspection())
        myFixture.allowTreeAccessForAllFiles()
    }


    @Test
    fun noEscapeViaFunction() {
        myFixture.testHighlighting("NoEscapeViaFunction.kt")
    }

    @Test
    fun noEscapeViaLambda() {
        myFixture.testHighlighting("NoEscapeViaLambda.kt")
    }

    @Test
    fun noEscapeViaVariable() {
        myFixture.testHighlighting("NoEscapeViaVariable.kt")
    }

    //TODO ignore?
    @Test
    fun noEscapeTrackValidContexts(){
        myFixture.testHighlighting("NoEscapeTrackValidContexts.kt")
    }
}
