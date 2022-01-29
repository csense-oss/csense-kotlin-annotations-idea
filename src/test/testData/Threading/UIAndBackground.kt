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
public annotation class InBackground




object UIAndBackground {

    @InBackground
    fun fromBackground(){
        <error descr="Trying to access a `UI thread` from a `Background / worker thread`">inUi</error>()
    }

    @InUi
    fun inUi(){

    }

    @InUi
    fun fromUi(){
        <error descr="Trying to access a `Background / worker thread` from a `UI thread`">intoBackground</error>()
    }

    @InBackground
    fun intoBackground(){

    }
}