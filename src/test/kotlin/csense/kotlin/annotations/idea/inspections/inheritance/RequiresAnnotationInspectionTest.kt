package csense.kotlin.annotations.idea.inspections.inheritance

import csense.idea.kotlin.test.*
import org.junit.*

class RequiresAnnotationInspectionTest : KotlinLightCodeInsightFixtureTestCaseJunit4() {
    override fun getTestDataPath(): String {
        return "src/test/testData/RequiresAnnotation/"
    }


    @Before
    fun setup() {
        myFixture.enableInspections(RequiresAnnotationInspection())
        myFixture.allowTreeAccessForAllFiles()
    }


    @Test
    fun abstractBase() {
        myFixture.testHighlighting("AbstractBaseClass.kt")
    }
    
    @Test
    fun openBase() {
        myFixture.testHighlighting("OpenBaseClass.kt")
    }
    
    @Test
    fun interfaceBase() {
        myFixture.testHighlighting("InterfaceBase.kt")
    }

}