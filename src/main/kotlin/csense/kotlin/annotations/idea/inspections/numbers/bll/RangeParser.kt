//package csense.kotlin.annotations.idea.inspections.numbers.bll
//
//import csense.idea.base.bll.kotlin.*
//import csense.kotlin.annotations.idea.*
//import org.intellij.lang.annotations.Language
//import org.jetbrains.kotlin.idea.intentions.branchedTransformations.*
//import org.jetbrains.kotlin.psi.*
//import org.jetbrains.uast.*
//
////TODO parse / use
////https://github.com/JetBrains/java-annotations/blob/master/java8/src/main/java/org/jetbrains/annotations/Range.java
//// and the android editions. from both packages (androidx.annotations & android.support.annotation)
//
////Only assumption: from and to
//sealed class RangeParser<T : Number>(
//    val allowDifferentArgumentTypesThanAnnotating: Boolean,
//    val annotationNames: Set<String>,
//    val minValue: T,
//    val maxValue: T,
//    val allowedTypeNames: List<String>,
//    val parseValue: (UExpression) -> T?,
//    val parseValueKt: (KtExpression) -> T?,
//    val isInRange: (from: T, to: T, value: T) -> Boolean,
//    val isEqual: (from: T, to: T) -> Boolean,
//    val isGreaterThan: (from: T, to: T) -> Boolean
//) {
//
//
//    //TODO Unsigned numbers
//
//    //whole numbers
//    object ByteRangeParser : RangeParser<Byte>(
//        allowDifferentArgumentTypesThanAnnotating = false,
//        annotationNames = setOf("ByteLimit"),
//        minValue = Byte.MIN_VALUE,
//        maxValue = Byte.MAX_VALUE,
//        allowedTypeNames = listOf("Byte", "Byte?"),
//        parseValue = UExpression::asByte,
//        parseValueKt = KtExpression::asByte,
//        isInRange = { from: Byte, to: Byte, value: Byte -> value in from..to },
//        isEqual = { from: Byte, to: Byte -> from == to },
//        isGreaterThan = { from: Byte, to: Byte -> from > to })
//
//    object ShortRangeParser : RangeParser<Short>(
//        allowDifferentArgumentTypesThanAnnotating = false,
//        annotationNames = setOf("ShortLimit"),
//        minValue = Short.MIN_VALUE,
//        maxValue = Short.MAX_VALUE,
//        allowedTypeNames = listOf("Short", "Short?"),
//        parseValue = UExpression::asShort,
//        parseValueKt = KtExpression::asShort,
//        isInRange = { from: Short, to: Short, value: Short -> value in from..to },
//        isEqual = { from: Short, to: Short -> from == to },
//        isGreaterThan = { from: Short, to: Short -> from > to })
//
//    object IntRangeParser : RangeParser<Int>(
//        allowDifferentArgumentTypesThanAnnotating = false,
//        annotationNames = setOf("IntLimit", "Range"),
//        minValue = Int.MIN_VALUE,
//        maxValue = Int.MAX_VALUE,
//        allowedTypeNames = listOf("Int", "Int?"),
//        parseValue = UExpression::asInt,
//        parseValueKt = KtExpression::asInt,
//        isInRange = { from: Int, to: Int, value: Int -> value in from..to },
//        isEqual = { from: Int, to: Int -> from == to },
//        isGreaterThan = { from: Int, to: Int -> from > to })
//
//
//    object LongRangeParser : RangeParser<Long>(
//        allowDifferentArgumentTypesThanAnnotating = false,
//        annotationNames = setOf("LongLimit", "Range"), //range is from jetbrains annotations
//        minValue = Long.MIN_VALUE,
//        maxValue = Long.MAX_VALUE,
//        allowedTypeNames = listOf("Long", "Long?"),
//        parseValue = UExpression::asLong,
//        parseValueKt = KtExpression::asLong,
//        isInRange = { from: Long, to: Long, value: Long -> value in from..to },
//        isEqual = { from: Long, to: Long -> from == to },
//        isGreaterThan = { from: Long, to: Long -> from > to })
//
//
//    //float /double
//    object FloatRangeParser : RangeParser<Float>(
//        allowDifferentArgumentTypesThanAnnotating = false,
//        annotationNames = setOf("FloatLimit", "Range"), //range is from jetbrains annotations
//        minValue = Float.MIN_VALUE,
//        maxValue = Float.MAX_VALUE,
//        allowedTypeNames = listOf("Float", "Float?"),
//        parseValue = UExpression::asFloat,
//        parseValueKt = KtExpression::asFloat,
//        isInRange = { from: Float, to: Float, value: Float -> value in from..to },
//        isEqual = { from: Float, to: Float -> from == to },
//        isGreaterThan = { from: Float, to: Float -> from > to })
//
//
//    object DoubleRangeParser : RangeParser<Double>(
//        allowDifferentArgumentTypesThanAnnotating = false,
//        annotationNames = setOf("DoubleLimit", "Range"), //range is from jetbrains annotations
//        minValue = Double.MIN_VALUE,
//        maxValue = Double.MAX_VALUE,
//        allowedTypeNames = listOf("Double", "Double?"),
//        parseValue = UExpression::asDouble,
//        parseValueKt = KtExpression::asDouble,
//        isInRange = { from: Double, to: Double, value: Double -> value in from..to },
//        isEqual = { from: Double, to: Double -> from == to },
//        isGreaterThan = { from: Double, to: Double -> from > to })
//
//    object AndroidIntRange : RangeParser<Long>(
//        allowDifferentArgumentTypesThanAnnotating = true,
//        annotationNames = setOf("IntRange"),
//        minValue = Long.MIN_VALUE,
//        maxValue = Long.MAX_VALUE,
//        allowedTypeNames = listOf("Long", "Int", "Long?", "Int?"),
//        parseValue = UExpression::asLong,
//        parseValueKt = KtExpression::asLong,
//        isInRange = { from: Long, to: Long, value: Long -> value in from..to },
//        isEqual = { from: Long, to: Long -> from == to },
//        isGreaterThan = { from: Long, to: Long -> from > to })
//
//    object AndroidFloatRange : RangeParser<Double>(
//        allowDifferentArgumentTypesThanAnnotating = true,
//        annotationNames = setOf("FloatRange"),
//        minValue = Double.MIN_VALUE,
//        maxValue = Double.MAX_VALUE,
//        allowedTypeNames = listOf("Double", "Float", "Double?", "Float?"),
//        parseValue = UExpression::asDouble,
//        parseValueKt = KtExpression::asDouble,
//        isInRange = { from: Double, to: Double, value: Double -> value in from..to },
//        isEqual = { from: Double, to: Double -> from == to },
//        isGreaterThan = { from: Double, to: Double -> from > to })
//
//
//    companion object {
//
//        fun parse(argAnnotations: List<UAnnotation?>): RangeParser<*>? = when {
//            //android
//            AndroidFloatRange.isThis(argAnnotations) -> AndroidFloatRange
//            AndroidIntRange.isThis(argAnnotations) -> AndroidIntRange
//            //regular
//            ByteRangeParser.isThis(argAnnotations) -> ByteRangeParser
//            ShortRangeParser.isThis(argAnnotations) -> ShortRangeParser
//            IntRangeParser.isThis(argAnnotations) -> IntRangeParser
//            LongRangeParser.isThis(argAnnotations) -> LongRangeParser
//            FloatRangeParser.isThis(argAnnotations) -> FloatRangeParser
//            DoubleRangeParser.isThis(argAnnotations) -> DoubleRangeParser
//
//            else -> null
//        }
//
//        fun parseKt(argAnnotations: List<KtAnnotationEntry?>): RangeParser<*>? = when {
//            //android
//            AndroidFloatRange.isThisKt(argAnnotations) -> AndroidFloatRange
//            AndroidIntRange.isThisKt(argAnnotations) -> AndroidIntRange
//            //regular
//            ByteRangeParser.isThisKt(argAnnotations) -> ByteRangeParser
//            ShortRangeParser.isThisKt(argAnnotations) -> ShortRangeParser
//            IntRangeParser.isThisKt(argAnnotations) -> IntRangeParser
//            LongRangeParser.isThisKt(argAnnotations) -> LongRangeParser
//            FloatRangeParser.isThisKt(argAnnotations) -> FloatRangeParser
//            DoubleRangeParser.isThisKt(argAnnotations) -> DoubleRangeParser
//            else -> null
//        }
//
//    }
//
//    fun isThisKt(annotations: List<KtAnnotationEntry?>): Boolean = annotations.findThisRangeAnnotation() != null
//
//    fun List<KtAnnotationEntry?>.findThisRangeAnnotation(): KtAnnotationEntry? = find {
//        it != null && it.valueArguments.size <= 2 && annotationNames.contains(it.shortName?.asString())
//    }
//
//    fun isThis(values: List<UAnnotation?>): Boolean = values.findThisRangeAnnotation() != null
//
//    fun List<UAnnotation?>.findThisRangeAnnotation(): UAnnotation? = find {
//        it != null && it.attributeValues.size <= 2 && annotationNames.contains(
//            it.namePsiElement?.text ?: ""
//        )
//    }
//
//    fun computeErrorMessage(argAnnotations: List<UAnnotation?>, valueArgument: KtValueArgument): String? {
//        val expression = valueArgument.getArgumentExpression() ?: run {
//            //"Failed to get argument expression"
//            return@computeErrorMessage null
//        }
//        return computeErrorMessage(argAnnotations, expression)
//    }
//
//    fun computeErrorMessage(argAnnotations: List<UAnnotation?>, expression: KtExpression): String? {
//        val annotation = argAnnotations.findThisRangeAnnotation() ?: run {
//            // "(error: could not find this)"
//            return@computeErrorMessage null
//        }
//        val asUExpression = expression.toUElementOfType<UExpression>() ?: run {
//            //"(error: could not resolve as UExpression)"
//            return@computeErrorMessage null
//        }
//        val range = annotation.asRangePair(minValue = minValue, maxValue = maxValue, parseValue = parseValue) ?: run {
//            // "(error: could not read the range)"
//            return@computeErrorMessage null
//        }
//        val value = parseValue(asUExpression) ?: run {
//            //"(error: could not parse the value expression)"
//            return@computeErrorMessage null
//        }
//        return "<html><b color=\"#FF846A\">$value</b> is not in range:[ <b color=\"#3FC8CA\">${range.from}</b> ; <b color=\"#41CA3F\">${range.to}</b> ]</html>"
//    }
//
//
//    fun validateOrError(
//        annotations: List<KtAnnotationEntry>,
//        mayBeNull: Boolean,
//        valueExpression: KtExpression
//    ): String? {
//
//        val genericError = "[failed to parse]"
//        val numberAnnotation = annotations.findThisRangeAnnotation() ?: return genericError
//        val annotationRange = numberAnnotation.asRangePair(minValue, maxValue, parseValueKt) ?: return genericError
//
//        val functionCallRangeInvalid = valueExpression.tryValidateFunctionalRangeOrErrorRange(annotationRange)
//        if (functionCallRangeInvalid != null) {
//            @Language("html")
//            val errorMessage = """
//                |<html><b color="#FF846A"> $functionCallRangeInvalid </b> is not in range : [ <b color="#3FC8CA">${annotationRange.from}</b> ; <b color="#41CA3F">${annotationRange.to}</b> ]</html>
//            """.trimMargin()
//            return errorMessage
//        }
//
//        //TODO all unparseable things will end "here". eg lambda args etc.
//        val parsedArgumentValue = parseValueKt(valueExpression) ?: return null
//
//        if (!isInRange(
//                annotationRange.from,
//                annotationRange.to,
//                parsedArgumentValue
//            )
//        ) {
//            @Language("html")
//            val errorMessage = """
//                |<html><b color="#FF846A">$parsedArgumentValue</b> is not in range : [ <b color="#3FC8CA">${annotationRange.from}</b> ; <b color="#41CA3F">${annotationRange.to}</b> ]</html>
//            """.trimMargin()
//            return errorMessage
//        }
//        return null
//    }
//
//    fun isValid(argAnnotations: List<UAnnotation?>, valueArgument: KtValueArgument): Boolean {
//        val argumentExpression = valueArgument.getArgumentExpression() ?: return false
//        return isValid(argAnnotations, argumentExpression)
//    }
//
//    fun isValid(argAnnotations: List<UAnnotation?>, argumentExpression: KtExpression): Boolean {
//        val annotation = argAnnotations.findThisRangeAnnotation() ?: return false
//        val range = annotation.asRangePair(minValue, maxValue, parseValue) ?: return false
//        val asUExpression = argumentExpression.toUElementOfType<UExpression>()
//            ?: return false
//        val value = parseValue(asUExpression)
//            ?: return true //well since we are not doing any "deep" analysis, we just assume any complex expression is ok.
//        //this could be expanded to look at the given value (it might be annotated as well) and verify if its range is in this range.
//        //if it is a math expression then we would have to do a deep analysis, which is quite complex.
//        return isInRange(range.from, range.to, value)
//    }
//
//    fun computeInvalidRangeMessageKt(argAnnotations: List<KtAnnotationEntry?>): String? {
//        val annotation = argAnnotations.findThisRangeAnnotation() ?: return null
//        val range = annotation.asRangePair(minValue, maxValue, parseValueKt) ?: return null
//        return range.computeInvalidMessage()
//    }
//
//    fun computeInvalidRangeMessage(argAnnotations: List<UAnnotation?>): String? {
//        val annotation = argAnnotations.findThisRangeAnnotation() ?: return null
//        val range = annotation.asRangePair(minValue, maxValue, parseValue) ?: return null
//        return range.computeInvalidMessage()
//    }
//
//    private fun RangePair<T>.computeInvalidMessage(): String? {
//        if (isEqual(from, to)) {
//            return "Range is invalid, as no value exists between from and to(${from};${to})"
//        }
//        return when {
//            isGreaterThan(from, to) -> "Range is properly swapped as from is greater than to (${from}; ${to})"
//            else -> null
//        }
//    }
//
//    fun isRangeReveresed(argAnnotations: List<UAnnotation?>): Boolean {
//        val annotation = argAnnotations.findThisRangeAnnotation() ?: return false
//        val range = annotation.asRangePair(minValue, maxValue, parseValue) ?: return false
//        return isGreaterThan(range.from, range.to)
//    }
//
//    fun isRangeReveresedKt(argAnnotations: List<KtAnnotationEntry?>): Boolean {
//        val anno = argAnnotations.findThisRangeAnnotation() ?: return false
//        val range = anno.asRangePair(minValue, maxValue, parseValueKt) ?: return false
//        return isGreaterThan(range.from, range.to)
//    }
//
//    fun findAnnotation(elements: List<UAnnotation?>): UAnnotation? =
//        elements.findThisRangeAnnotation()
//
//    fun findAnnotationKt(elements: List<KtAnnotationEntry?>): KtAnnotationEntry? =
//        elements.findThisRangeAnnotation()
//
//
//}
//
//fun KtExpression.tryValidateFunctionalRangeOrErrorRange(expectedRangePair: RangePair<*>): String? {
//    if (this is KtCallExpression) {
//        val fnc = this.resolveMainReferenceAsKtFunction() ?: return null
//        val range = RangeParser.parseKt(fnc.annotationEntries) ?: return null
//        val numberAnnotation = range.findAnnotationKt(fnc.annotationEntries) ?: return null
//        val functionRange = numberAnnotation.asRangePair(
//            minValue = range.minValue,
//            maxValue = range.maxValue,
//            parseValue = range.parseValueKt
//        ) ?: return null
//        val isValidRange = functionRange.isRangeContainedIn(expectedRangePair)
//        if (!isValidRange) {
//            return "[ ${functionRange.from} ; ${functionRange.to} ]"
//        }
//        return null
//    }
//    return null
//}
//
//fun RangePair<*>.isRangeContainedIn(other: RangePair<*>): Boolean {
//    return from >= other.from && to <= other.to
//}