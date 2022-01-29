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



object UIAndNone {
    //TODO should be highlighted? warning etc?
    fun none(){
        inUi()
    }

    @InUi
    fun inUi(){

    }

    @InUi
    fun fromUi(){
        intoNone()
    }


    fun intoNone(){

    }
}