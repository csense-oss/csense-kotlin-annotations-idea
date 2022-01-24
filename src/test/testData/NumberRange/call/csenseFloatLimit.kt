@file:Suppress("unused")

package csense.kotlin.annotations.numbers

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.ANNOTATION_CLASS
)

@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class FloatLimit(
    val from: Float = Float.MIN_VALUE,
    val to: Float = Float.MAX_VALUE
)

fun requireRange(@FloatLimit(-1F, 600F) x: Float){
    if(x>20 || x < 0){

    }
}

fun useRange(){
    requireRange(<error descr="-2.0 is not in range [-1.0;600.0]">-2F</error>)
    requireRange(-1F)
    requireRange(0F)
    requireRange(20F)
    requireRange(500F)
    requireRange(600F)
    requireRange(<error descr="8000.0 is not in range [-1.0;600.0]">8000F</error>)
    requireRange(<error descr="-1000.0 is not in range [-1.0;600.0]">-1000F</error>)
}