@file:Suppress("unused")

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
public annotation class ByteLimit(
    val from: Byte = Byte.MIN_VALUE,
    val to: Byte = Byte.MAX_VALUE
)

fun requireRange(@ByteLimit(0.toByte(), 20.toByte()) x: Byte){
    if(x>20 || x < 0){

    }
}

fun useRange(){
    requireRange(<error descr="-1 is not in range [0;20]">-1</error>)
    requireRange(0)
    requireRange(20)
    requireRange(<error descr="21 is not in range [0;20]">21</error>)
    requireRange(<error descr="-6 is not in range [0;20]">250.toByte()</error>)
}