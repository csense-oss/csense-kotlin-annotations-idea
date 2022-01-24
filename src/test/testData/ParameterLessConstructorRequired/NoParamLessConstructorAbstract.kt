package csense.kotlin.annotations.inheritance

annotation class ParameterLessConstructorRequired

@ParameterLessConstructorRequired
abstract class NoParamLessConstructorAbstract {

}

class <error descr="You need a parameter less constructor (as per the ParameterLessConstructor interface)">NoParamLessConstructor</error> : NoParamLessConstructorAbstract {
    constructor(x: Int) {

    }
}