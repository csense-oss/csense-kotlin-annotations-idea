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
    <error descr="Getter is properly not constant.">override val X: Int
        get() = computeX()</error>

    fun computeX(): Int {
        return Int.MAX_VALUE * java.lang.Math.random().toInt()
    }
}

class UseIfaceFailure2 : Iface {
    <error descr="Getter is properly not constant.">override val X: Int
        get() = Int.MAX_VALUE * java.lang.Math.random().toInt()</error>
}