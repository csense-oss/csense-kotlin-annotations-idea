package csense.kotlin.annotations.idea.inspections.inheritance

import csense.idea.kotlin.test.*
import org.junit.*

class ParameterLessConstructorRequriedInspectionTest: KotlinLightCodeInsightFixtureTestCaseJunit4() {
    override fun getTestDataPath(): String {
        return "src/test/testData/ParameterLessConstructorRequired/"
    }
    @Before
    fun setup() {
        myFixture.enableInspections(ParameterLessConstructorRequriedInspection())
        myFixture.allowTreeAccessForAllFiles()
    }


    @Test
    fun primaryConstructor() {
        myFixture.testHighlighting("NoParamLessConstructorsPrimary.kt")
    }

    @Test
    fun secondaryConstructor() {
        myFixture.testHighlighting("NoParamLessConstructorsSecondary.kt")
    }

    @Test
    fun openClass() {
        myFixture.testHighlighting("NoParamLessConstructorOpen.kt")
    }

    @Test
    fun abstractClass() {
        myFixture.testHighlighting("NoParamLessConstructorAbstract.kt")
    }

    @Test
    fun regularClass() {
        myFixture.testHighlighting("NoParamLessClassMissing.kt")
    }


    @Test
    fun okClass() {
        myFixture.testHighlighting("NoParamLessClassOk.kt")
    }

    @Test
    fun okMixedClass() {
        myFixture.testHighlighting("NoParamLessClassMixed.kt")
    }


}