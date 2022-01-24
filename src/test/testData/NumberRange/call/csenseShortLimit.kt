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
public annotation class ShortLimit(
    val from: Short = Short.MIN_VALUE,
    val to: Short = Short.MAX_VALUE
)
fun requireRange(@ShortLimit(0.toShort(), 80.toShort()) x: Short){
    if(x>20 || x < 0){

    }
}

fun useRange(){
    requireRange(<error descr="-1 is not in range [0;80]">-1</error>)
    requireRange(0)
    requireRange(20)
    requireRange(50)
    requireRange(80)
    requireRange(<error descr="81 is not in range [0;80]">81</error>)
    requireRange(<error descr="250 is not in range [0;80]">250.toShort()</error>)
}