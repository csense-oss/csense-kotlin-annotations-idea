package csense.kotlin.annotations.inheritance

annotation class ParameterLessConstructorRequired

@ParameterLessConstructorRequired
class <error descr="You need a parameter less constructor (as per the ParameterLessConstructor interface)">MissingConstructor</error>(val x: Int) {

}