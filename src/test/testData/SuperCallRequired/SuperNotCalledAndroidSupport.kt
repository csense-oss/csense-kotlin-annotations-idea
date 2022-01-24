package android.support.annotation
annotation class CallSuper

open class AndroidSupportSuper{
    @CallSuper
    open fun callMe(){}
}

class ChildAndroidSupport(): AndroidSupportSuper(){
    override fun <error descr="You do not call super on an overridden method annotated to require a super call">callMe</error>() {

    }
}