package csense.kotlin.annotations.inheritance

annotation class ParameterLessConstructorRequired

@ParameterLessConstructorRequired
open class NoParamLessConstructorOpen {

}

class <error descr="You need a parameter less constructor (as per the ParameterLessConstructor interface)">NoParamLessConstructorOpenChild</error>(
    val x: Int
) : NoParamLessConstructorOpen() {

}