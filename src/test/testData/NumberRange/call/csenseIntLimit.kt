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
public annotation class IntLimit(
    val from: Int = Int.MIN_VALUE,
    val to: Int = Int.MAX_VALUE
)

fun requireRange(@IntLimit(-1, 6500) x: Int){
    if(x>20 || x < 0){

    }
}

fun useRange(){
    requireRange(<error descr="-2 is not in range [-1;6500]">-2</error>)
    requireRange(-1)
    requireRange(0)
    requireRange(20)
    requireRange(2000)
    requireRange(6500)
    requireRange(<error descr="8000 is not in range [-1;6500]">8000</error>)
    requireRange(<error descr="-1000 is not in range [-1;6500]">-1000</error>)
}