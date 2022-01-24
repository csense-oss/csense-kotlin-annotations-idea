@file:Suppress("unused")

package csense.kotlin.annotations.properties

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
public annotation class PropertyMustBeConstant

interface Iface {
    @PropertyMustBeConstant
    val X: Int
}

class UseIface : Iface {
    override val X: Int
        get() = 42
}

class Math : Iface {
    override val X: Int = 45 - 87
}

class MathGetter : Iface {
    override val X: Int
        get() = 45 * 87
}