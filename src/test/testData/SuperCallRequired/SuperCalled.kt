open class SuperCalled {
    open fun x() {}
}

class ChildNotCallingSuper : SuperCalled() {
    override fun x() {
        super.x()
    }
}