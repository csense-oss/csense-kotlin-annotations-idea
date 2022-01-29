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


class NoEscapeViaVariable {
    @NoEscape
    val x: Long
        get() = java.lang.System.currentTimeMillis()


    fun failX(){
        <error descr="This is marked NoEscape; assignment prohibited.">val z = x</error>
        if(z > 0) {

        }
    }

}