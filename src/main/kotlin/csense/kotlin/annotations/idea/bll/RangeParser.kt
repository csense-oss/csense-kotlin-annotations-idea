package csense.kotlin.annotations.idea.bll

import csense.idea.base.bll.kotlin.isConstant
import csense.kotlin.annotations.idea.inspections.*
import org.jetbrains.kotlin.idea.analysis.analyzeInContext
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.js.descriptorUtils.nameIfStandardType
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.namePsiElement
import org.jetbrains.uast.toUElementOfType
//TODO parse / use
//https://github.com/JetBrains/java-annotations/blob/master/java8/src/main/java/org/jetbrains/annotations/Range.java
// and the android editions. from both packages (androidx.annotations & android.support.annotation)

//Only assumtion: from and to
sealed class RangeParser<T>(
        val annotationNames: Set<String>,
        val minValue: T,
        val maxValue: T,
        val typeName: String,
        val parseValue: (UExpression) -> T?,
        val parseValueKt: (KtExpression) -> T?,
        val isInRange: (from: T, to: T, value: T) -> Boolean,
        val isEqual: (from: T, to: T) -> Boolean,
        val isGreaterThan: (from: T, to: T) -> Boolean
) {


    //TODO Unsigned numbers

    //whole numbers
    object ByteRangeParser : RangeParser<Byte>(
            setOf("ByteLimit"), //range is from jetbrains annotations
            Byte.MIN_VALUE,
            Byte.MAX_VALUE,
            "Byte",
            UExpression::asByte,
            KtExpression::asByte,
            { from: Byte, to: Byte, value: Byte -> value in from..to },
            { from: Byte, to: Byte -> from == to },
            { from: Byte, to: Byte -> from > to })

    object ShortRangeParser : RangeParser<Short>(
            setOf("ShortLimit"), //range is from jetbrains annotations
            Short.MIN_VALUE,
            Short.MAX_VALUE,
            "Short",
            UExpression::asShort,
            KtExpression::asShort,
            { from: Short, to: Short, value: Short -> value in from..to },
            { from: Short, to: Short -> from == to },
            { from: Short, to: Short -> from > to })

    object IntRangeParser : RangeParser<Int>(
            setOf("IntRange", "IntLimit", "Range"), //range is from jetbrains annotations
            Int.MIN_VALUE,
            Int.MAX_VALUE,
            "Int",
            UExpression::asInt,
            KtExpression::asInt,
            { from: Int, to: Int, value: Int -> value in from..to },
            { from: Int, to: Int -> from == to },
            { from: Int, to: Int -> from > to })


    object LongRangeParser : RangeParser<Long>(
            setOf("IntRange", "LongLimit", "Range"), //range is from jetbrains annotations
            Long.MIN_VALUE,
            Long.MAX_VALUE,
            "Long",
            UExpression::asLong,
            KtExpression::asLong,
            { from: Long, to: Long, value: Long -> value in from..to },
            { from: Long, to: Long -> from == to },
            { from: Long, to: Long -> from > to })


    //float /double
    object FloatRangeParser : RangeParser<Float>(
            setOf("FloatRange", "FloatLimit", "Range"), //range is from jetbrains annotations
            Float.MIN_VALUE,
            Float.MAX_VALUE,
            "Float",
            UExpression::asFloat,
            KtExpression::asFloat,
            { from: Float, to: Float, value: Float -> value in from..to },
            { from: Float, to: Float -> from == to },
            { from: Float, to: Float -> from > to })


    object DoubleRangeParser : RangeParser<Double>(
            setOf("FloatRange", "DoubleLimit", "Range"), //range is from jetbrains annotations
            Double.MIN_VALUE,
            Double.MAX_VALUE,
            "Double",
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
                it.attributeValues.size <= 2 && annotationNames.contains(it.namePsiElement?.text ?: "")
    }

    fun computeErrorMessage(argAnnotations: List<UAnnotation?>, valueArgument: KtValueArgument): String {
        val annotation = argAnnotations.findThis() ?: return ""
        val asUExpression = valueArgument.getArgumentExpression()?.toUElementOfType<UExpression>() ?: return ""
        val range = annotation.asRangePair(minValue, maxValue, parseValue) ?: return ""
        val value = parseValue(asUExpression) ?: return ""
        return "$value is not in range [${range.from};${range.to}]"
    }


    fun validate(argAnnotations: List<UAnnotation?>, valueArgument: KtValueArgument): Boolean {
        val annotation = argAnnotations.findThis() ?: return false
        val range = annotation.asRangePair(minValue, maxValue, parseValue) ?: return false
        val asUExpression = valueArgument.getArgumentExpression()?.toUElementOfType<UExpression>() ?: return false
        val value = parseValue(asUExpression) ?: return false
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

    fun verifyTypeName(param: KtParameter): Boolean {
        val paramTypeName = param.typeReference?.text
        return this.typeName == paramTypeName
    }

    fun verifyTypeName(param: KtExpression): Boolean {
        val bindingContext = param.analyze(BodyResolveMode.PARTIAL)
        val type =  param.getType(bindingContext)?.nameIfStandardType?.asString()
        return this.typeName == type
    }

    fun findAnnotation(elements: List<UAnnotation?>): UAnnotation? =
            elements.findThis()
    fun findAnnotationKt(elements: List<KtAnnotationEntry?>): KtAnnotationEntry? =
            elements.findThis()
}