package csense.kotlin.annotations.inheritance

annotation class ParameterLessConstructorRequired

@ParameterLessConstructorRequired
class OkMixed(val x: Int) {
    constructor(): this(42) {

    }
}