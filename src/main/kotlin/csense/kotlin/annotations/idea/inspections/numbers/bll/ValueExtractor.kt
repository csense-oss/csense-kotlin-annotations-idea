package csense.kotlin.annotations.idea.inspections.numbers.bll

import org.jetbrains.kotlin.psi.*
import org.jetbrains.uast.*


fun UExpression.asLong(): Long? = asT(String::toLongOrNull)
fun UExpression.asInt(): Int? = asT(String::toIntOrNull)
fun UExpression.asDouble(): Double? = asT(String::toDoubleOrNull)
fun UExpression.asFloat(): Float? = asT(String::toFloatOrNull)
fun UExpression.asByte(): Byte? = asT(String::toByteOrNull)
fun UExpression.asShort(): Short? = asT(String::toShortOrNull)

fun KtExpression.asLong(): Long? = asT(String::toLongOrNull)
fun KtExpression.asInt(): Int? = asT(String::toIntOrNull)
fun KtExpression.asDouble(): Double? = asT(String::toDoubleOrNull)
fun KtExpression.asFloat(): Float? = asT(String::toFloatOrNull)
fun KtExpression.asByte(): Byte? = asT(String::toByteOrNull)
fun KtExpression.asShort(): Short? = asT(String::toShortOrNull)

//this looks like a hack.... hmm
inline fun <reified T> UExpression.asT(converter: (String) -> T?): T? {
    val evaluated = this.evaluate()
    if (evaluated is T) {
        return evaluated
    }
    return evaluated?.toString()?.let(converter)
}

inline fun <reified T> KtExpression.asT(converter: (String) -> T?): T? {
    return when (this) {
        is KtPrefixExpression -> converter(this.text)
        is KtConstantExpression -> converter(this.text)
        else -> null
    }
}

