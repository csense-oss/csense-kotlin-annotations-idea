@file:Suppress("unused")

package csense.kotlin.annotations.threading

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE //for functional types
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class InUi

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE //for functional types
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class InAny




object UIAndBackground {

    @InAny
    fun fromAny(){
        <error descr="Trying to access a `UI thread` from a `Any thread`">inUi</error>()
    }

    @InUi
    fun inUi(){

    }

    @InUi
    fun fromUi(){
        intoAny()
    }

    @InAny
    fun intoAny(){

    }
}