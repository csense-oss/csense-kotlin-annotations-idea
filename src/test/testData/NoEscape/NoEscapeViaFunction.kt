@file:Suppress("unused")

package csense.kotlin.annotations.sideEffect

@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE_PARAMETER,
    AnnotationTarget.TYPE,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.LOCAL_VARIABLE
)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
public annotation class NoEscape

object NoEscapeViaFunction {

    private var someValue: String? = null
    fun x(@NoEscape b: String){
        someValue = <error descr="Assigning something marked NoEscape means it escapes">b</error>
        if(getUIContext()>42){
            <error descr="This is marked NoEscape; assignment prohibited.">val z = getUIContext()</error>
            if(z > 0) {

            }
        }
    }

    @NoEscape
    fun getUIContext(): Int{
        return 42
    }

}