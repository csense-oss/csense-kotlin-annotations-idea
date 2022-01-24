package csense.kotlin.annotations.inheritance

annotation class ParameterLessConstructorRequired

@ParameterLessConstructorRequired
class OkPrimary() {

}

@ParameterLessConstructorRequired
class OkSecondary {
    constructor() {

    }
}