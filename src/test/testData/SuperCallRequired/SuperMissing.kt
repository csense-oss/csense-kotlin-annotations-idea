package csense.kotlin.annotations.inheritance

annotation class SuperCallRequired()

abstract class SuperMissing {
    @SuperCallRequired
    open fun abc(){

    }
}

class ChildNotCallingSuper: SuperMissing() {
    override fun <error descr="You do not call super on an overridden method annotated to require a super call">abc</error>(){

    }
}