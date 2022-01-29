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

object NoEscapeViaLambda {

    fun onAccessToken(action: (accessToken: @NoEscape String) -> Unit) {
        action("test")
    }

    private var lastUsedAccessToken: String? = null
    fun useOnAccessToken() {
        onAccessToken {
            lastUsedAccessToken = <error descr="This is marked NoEscape; assignment prohibited.">it</error>
        }
    }

}