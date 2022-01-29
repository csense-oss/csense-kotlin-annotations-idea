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
public annotation class InBackground

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


object BackgroundAndAny {
    @InAny
    fun fromAny(){
        <error descr="Trying to access a `Background / worker thread` from a `Any thread`">inBackground</error>()
    }

    @InBackground
    fun inBackground(){

    }

    @InBackground
    fun fromBackground(){
        intoAny()
    }

    @InAny
    fun intoAny(){

    }

}