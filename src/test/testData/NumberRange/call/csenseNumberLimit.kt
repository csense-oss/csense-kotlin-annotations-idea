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
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.TYPE//until you can annotate functional parameters
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class NumberLimit(
    val from: Double = Double.MIN_VALUE,
    val to: Double = Double.MAX_VALUE
)

fun requireRange(@NumberLimit(-1.0, 500.0) x: Number){
    if(x.toInt() > 0){

    }
}

fun useRange(){
    requireRange(<error descr="-2 is not in range [-1.0;500.0]">-2</error>)
    requireRange(-1)
    requireRange(0)
    requireRange(20)
    requireRange(2000)
    requireRange(6500)
    requireRange(<error descr="8000.0 is not in range [-1.0;500.0]">8000</error>)
    requireRange(<error descr="-1000.0 is not in range [-1.0;500.0]">-1000</error>)
}