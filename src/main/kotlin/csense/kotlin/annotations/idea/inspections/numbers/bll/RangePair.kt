package csense.kotlin.annotations.idea.inspections.numbers.bll

import org.jetbrains.kotlin.psi.*
import org.jetbrains.uast.*


data class RangePair<T>(val from: T, val to: T)

fun <T> UAnnotation.asRangePair(
    minValue: T,
    maxValue: T,
    parseValue: (UExpression) -> T?
): RangePair<T>? {
    val fromValue: T
    val toValue: T
    when (attributeValues.size) {
        0 -> {
            fromValue = minValue
            toValue = maxValue
        }

        1 -> {
            val exp = attributeValues.first()

            if ("from".equals(exp.name, true)) {
                toValue = maxValue
                fromValue = parseValue(exp) ?: return null
            } else {
                fromValue = minValue
                toValue = parseValue(exp) ?: return null
            }
        }

        2 -> {
            fromValue = attributeValues.firstOrNull()?.let(parseValue) ?: return null
            toValue = attributeValues.lastOrNull()?.let(parseValue) ?: return null
        }

        else -> return null
    }
    return RangePair(fromValue, toValue)
}

fun <T> KtAnnotationEntry.asRangePair(
    minValue: T,
    maxValue: T,
    parseValue: (KtExpression) -> T?
): RangePair<T>? {
    //TODO should parse as if it was a function call [see QuickNumberRangeParameterCallInspection strategy]
    val fromValue: T
    val toValue: T
    when (valueArguments.size) {
        0 -> {
            fromValue = minValue
            toValue = maxValue
        }

        1 -> {
            val exp = valueArguments.first()
            val value = exp.getArgumentExpression() ?: return null
            if ("from".equals(exp.getArgumentName()?.asName?.asString(), true)) {
                toValue = maxValue
                fromValue = parseValue(value) ?: return null
            } else {
                fromValue = minValue
                toValue = parseValue(value) ?: return null
            }
        }

        2 -> {
            //quick hack... and unstable /bad code.
            if ("to".equals(valueArguments.firstOrNull()?.getArgumentName()?.asName?.asString(), ignoreCase = true)) {
                toValue = valueArguments.firstOrNull()?.getArgumentExpression()?.let(parseValue) ?: return null
                fromValue = valueArguments.lastOrNull()?.getArgumentExpression()?.let(parseValue) ?: return null
            } else {
                fromValue = valueArguments.firstOrNull()?.getArgumentExpression()?.let(parseValue) ?: return null
                toValue = valueArguments.lastOrNull()?.getArgumentExpression()?.let(parseValue) ?: return null
            }
        }

        else -> return null
    }
    return RangePair(fromValue, toValue)
}


