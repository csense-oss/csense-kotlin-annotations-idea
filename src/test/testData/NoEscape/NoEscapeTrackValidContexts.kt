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

object NoEscapeTrackValidContexts {

    private var someValue: String? = null
    fun x(@NoEscape b: String){
        val z = b //This should "be allowed" as we are not leaving the context. but it should be "tracked" as well.
        someValue = <error descr="Assigning something marked NoEscape means it escapes">z</error>
    }
    fun onAccessToken(action: (@NoEscape accessToken: String) -> Unit) {

    }

    private var lastUsedAccessToken: String? = null
    fun useOnAccessToken() {
        onAccessToken {
            val z = it //This should "be allowed" as we are not leaving the context.
            lastUsedAccessToken = z
        }
    }
}