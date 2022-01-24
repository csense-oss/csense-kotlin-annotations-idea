package csense.kotlin.annotations.inheritance

annotation class ParameterLessConstructorRequired

@ParameterLessConstructorRequired
interface X {

}

class <error descr="You need a parameter less constructor (as per the ParameterLessConstructor interface)">NoParamLessConstructors</error> : X {
    constructor(x: Int) {

    }
}