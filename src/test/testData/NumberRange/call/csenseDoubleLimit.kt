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
public annotation class DoubleLimit(
    val from: Double = Double.MIN_VALUE,
    val to: Double = Double.MAX_VALUE
)

fun requireRange(@DoubleLimit(-1.0, 25.5) x: Double){
    if(x>20 || x < 0){

    }
}

fun useRange(){
    requireRange(<error descr="-2.0 is not in range [-1.0;25.5]">-2.0</error>)
    requireRange(-1.0)
    requireRange(0.0)
    requireRange(20.0)
    requireRange(25.0)
    requireRange(25.4)
    requireRange(25.5)
    requireRange(<error descr="25.55 is not in range [-1.0;25.5]">25.55</error>)
    requireRange(<error descr="8000.0 is not in range [-1.0;25.5]">8000.0</error>)
    requireRange(<error descr="-1000.0 is not in range [-1.0;25.5]">-1000.0</error>)
}