@file:Suppress("unused","UNUSED_VARIABLE","UNUSED_ANONYMOUS_PARAMETER","UNUSED_PARAMETER")

package csense.kotlin.annotations.numbers

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.TYPE//until you can annotate functional parameters
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class IntLimit(
    val from: Int = Int.MIN_VALUE,
    val to: Int = Int.MAX_VALUE
)


object Hmm {
    const val str : String = "hmm"
}

public fun assertCalled(
    string: String = Hmm.str, 
    @IntLimit(from = 1) times: Int = 1,
    action: (callback: () -> Unit) -> Unit
) {
}

fun isTotallyFine() = assertCalled(times = 3) { shouldBeCalled ->
    val x = 42
}

fun isNotOk() = assertCalled(<error descr="0 is not in range [1;2147483647]">times = 0</error>) { shouldBeCalled ->
    val x = 42
}