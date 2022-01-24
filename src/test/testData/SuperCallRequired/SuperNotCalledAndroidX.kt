package androidx.annotation
annotation class CallSuper

open class AndroidXSuper{
    @CallSuper
    open fun callMe(){}
}

class ChildAndroidX(): AndroidXSuper(){
    override fun <error descr="You do not call super on an overridden method annotated to require a super call">callMe</error>() {

    }
}