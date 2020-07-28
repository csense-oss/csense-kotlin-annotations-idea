package csense.kotlin.annotations.idea.bll

import org.jetbrains.kotlin.psi.*
import org.jetbrains.uast.*

//TODO parse / use
//https://github.com/JetBrains/java-annotations/blob/master/java8/src/main/java/org/jetbrains/annotations/Range.java
// and the android editions. from both packages (androidx.annotations & android.support.annotation)

//Only assumtion: from and to
sealed class RangeParser<T>(
        val allowDifferentArgumentTypesThanAnnotating: Boolean,
        val annotationNames: Set<String>,
        val minValue: T,
        val maxValue: T,
        val allowedTypeNames: List<String>,
        val parseValue: (UExpression) -> T?,
        val parseValueKt: (KtExpression) -> T?,
        val isInRange: (from: T, to: T, value: T) -> Boolean,
        val isEqual: (from: T, to: T) -> Boolean,
        val isGreaterThan: (from: T, to: T) -> Boolean
) {


    //TODO Unsigned numbers

    //whole numbers
    object ByteRangeParser : RangeParser<Byte>(
            false,
            setOf("ByteLimit"), //range is from jetbrains annotations
            Byte.MIN_VALUE,
            Byte.MAX_VALUE,
            listOf("Byte"),
            UExpression::asByte,
            KtExpression::asByte,
            { from: Byte, to: Byte, value: Byte -> value in from..to },
            { from: Byte, to: Byte -> from == to },
            { from: Byte, to: Byte -> from > to })

    object ShortRangeParser : RangeParser<Short>(
            false,
            setOf("ShortLimit"), //range is from jetbrains annotations
            Short.MIN_VALUE,
            Short.MAX_VALUE,
            listOf("Short"),
            UExpression::asShort,
            KtExpression::asShort,
            { from: Short, to: Short, value: Short -> value in from..to },
            { from: Short, to: Short -> from == to },
            { from: Short, to: Short -> from > to })

    object IntRangeParser : RangeParser<Int>(
            false,
            setOf("IntRange", "IntLimit", "Range"), //range is from jetbrains annotations
            Int.MIN_VALUE,
            Int.MAX_VALUE,
            listOf("Int"),
            UExpression::asInt,
            KtExpression::asInt,
            { from: Int, to: Int, value: Int -> value in from..to },
            { from: Int, to: Int -> from == to },
            { from: Int, to: Int -> from > to })


    object LongRangeParser : RangeParser<Long>(
            false,
            setOf("LongLimit", "Range"), //range is from jetbrains annotations
            Long.MIN_VALUE,
            Long.MAX_VALUE,
            listOf("Long"),
            UExpression::asLong,
            KtExpression::asLong,
            { from: Long, to: Long, value: Long -> value in from..to },
            { from: Long, to: Long -> from == to },
            { from: Long, to: Long -> from > to })


    //float /double
    object FloatRangeParser : RangeParser<Float>(
            false,
            setOf("FloatLimit", "Range"), //range is from jetbrains annotations
            Float.MIN_VALUE,
            Float.MAX_VALUE,
            listOf("Float"),
            UExpression::asFloat,
            KtExpression::asFloat,
            { from: Float, to: Float, value: Float -> value in from..to },
            { from: Float, to: Float -> from == to },
            { from: Float, to: Float -> from > to })


    object DoubleRangeParser : RangeParser<Double>(
            false,
            setOf("FloatRange", "DoubleLimit", "Range"), //range is from jetbrains annotations
            Double.MIN_VALUE,
            Double.MAX_VALUE,
            listOf("Double"),
            UExpression::asDouble,
            KtExpression::asDouble,
            { from: Double, to: Double, value: Double -> value in from..to },
            { from: Double, to: Double -> from == to },
            { from: Double, to: Double -> from > to })

    object AndroidIntRange : RangeParser<Long>(
            true,
            setOf("IntRange"),
            Long.MIN_VALUE,
            Long.MAX_VALUE,
            listOf("Long", "Int"),
            UExpression::asLong,
            KtExpression::asLong,
            { from: Long, to: Long, value: Long -> value in from..to },
            { from: Long, to: Long -> from == to },
            { from: Long, to: Long -> from > to })

    object AndroidFloatRange : RangeParser<Double>(
            true,
            setOf("FloatRange"),
            Double.MIN_VALUE,
            Double.MAX_VALUE,
            listOf("Double", "Float"),
            UExpression::asDouble,
            KtExpression::asDouble,
            { from: Double, to: Double, value: Double -> value in from..to },
            { from: Double, to: Double -> from == to },
            { from: Double, to: Double -> from > to })


    companion object {

        fun parse(argAnnotations: List<UAnnotation?>): RangeParser<*>? {
            return when {
                ByteRangeParser.isThis(argAnnotations) -> ByteRangeParser
                ShortRangeParser.isThis(argAnnotations) -> ShortRangeParser
                IntRangeParser.isThis(argAnnotations) -> IntRangeParser
                LongRangeParser.isThis(argAnnotations) -> LongRangeParser
                FloatRangeParser.isThis(argAnnotations) -> FloatRangeParser
                DoubleRangeParser.isThis(argAnnotations) -> DoubleRangeParser
                //android
                AndroidFloatRange.isThis(argAnnotations) -> AndroidFloatRange
                AndroidIntRange.isThis(argAnnotations) -> AndroidIntRange
                else -> null
            }
        }

        fun parseKt(argAnnotations: List<KtAnnotationEntry?>): RangeParser<*>? {
            return when {
                ByteRangeParser.isThisKt(argAnnotations) -> ByteRangeParser
                ShortRangeParser.isThisKt(argAnnotations) -> ShortRangeParser
                IntRangeParser.isThisKt(argAnnotations) -> IntRangeParser
                LongRangeParser.isThisKt(argAnnotations) -> LongRangeParser
                FloatRangeParser.isThisKt(argAnnotations) -> FloatRangeParser
                DoubleRangeParser.isThisKt(argAnnotations) -> DoubleRangeParser
                else -> null
            }
        }
    }

    fun isThisKt(annotations: List<KtAnnotationEntry?>): Boolean = annotations.findThis() != null

    fun List<KtAnnotationEntry?>.findThis(): KtAnnotationEntry? = find {
        it != null &&
                it.valueArguments.size <= 2 && annotationNames.contains(it.shortName?.asString())
    }

    fun isThis(values: List<UAnnotation?>): Boolean = values.findThis() != null

    fun List<UAnnotation?>.findThis(): UAnnotation? = find {
        it != null &&
                it.attributeValues.size <= 2 && annotationNames.contains(it.namePsiElement?.text
                ?: "")
    }

    fun computeErrorMessage(argAnnotations: List<UAnnotation?>, valueArgument: KtValueArgument): String {
        val annotation = argAnnotations.findThis() ?: return ""
        val asUExpression = valueArgument.getArgumentExpression()?.toUElementOfType<UExpression>()
                ?: return ""
        val range = annotation.asRangePair(minValue, maxValue, parseValue) ?: return ""
        val value = parseValue(asUExpression) ?: return ""
        return "$value is not in range [${range.from};${range.to}]"
    }


    fun validate(argAnnotations: List<UAnnotation?>, valueArgument: KtValueArgument): Boolean {
        val annotation = argAnnotations.findThis() ?: return false
        val range = annotation.asRangePair(minValue, maxValue, parseValue) ?: return false
        val asUExpression = valueArgument.getArgumentExpression()?.toUElementOfType<UExpression>()
                ?: return false
        val value = parseValue(asUExpression)
                ?: return true //well since we are not doing any "deep" analysis, we just assume any complex expression is ok.
        //this could be expanded to look at the given value (it might be annotated as well) and verify if its range is in this range.
        //if it is a math expression then we would have to do a deep analysis, which is quite complex.
        return isInRange(range.from, range.to, value)
    }

    fun computeInvalidRangeMessageKt(argAnnotations: List<KtAnnotationEntry?>): String? {
        val annotation = argAnnotations.findThis() ?: return null
        val range = annotation.asRangePair(minValue, maxValue, parseValueKt) ?: return null
        return range.computeInvalidMessage()
    }

    fun computeInvalidRangeMessage(argAnnotations: List<UAnnotation?>): String? {
        val annotation = argAnnotations.findThis() ?: return null
        val range = annotation.asRangePair(minValue, maxValue, parseValue) ?: return null
        return range.computeInvalidMessage()
    }

    private fun RangePair<T>.computeInvalidMessage(): String? {
        if (isEqual(from, to)) {
            return "Range is invalid, as no value exists between from and to(${from};${to})"
        }
        return when {
            isGreaterThan(from, to) -> "Range is properly swapped as from is greater than to (${from}; ${to})"
            else -> null
        }
    }

    fun isRangeReveresed(argAnnotations: List<UAnnotation?>): Boolean {
        val annotation = argAnnotations.findThis() ?: return false
        val range = annotation.asRangePair(minValue, maxValue, parseValue) ?: return false
        return isGreaterThan(range.from, range.to)
    }

    fun isRangeReveresedKt(argAnnotations: List<KtAnnotationEntry?>): Boolean {
        val anno = argAnnotations.findThis() ?: return false
        val range = anno.asRangePair(minValue, maxValue, parseValueKt) ?: return false
        return isGreaterThan(range.from, range.to)
    }

    fun findAnnotation(elements: List<UAnnotation?>): UAnnotation? =
            elements.findThis()

    fun findAnnotationKt(elements: List<KtAnnotationEntry?>): KtAnnotationEntry? =
            elements.findThis()
}