package csense.kotlin.annotations.idea

import com.intellij.codeInsight.*
import com.intellij.psi.*
import csense.idea.base.annotations.*
import csense.idea.base.bll.kotlin.*
import csense.kotlin.extensions.*
import csense.kotlin.extensions.collections.*
import csense.kotlin.extensions.collections.map.*
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.asJava.elements.*
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.idea.caches.resolve.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.lazy.descriptors.*
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.typeUtil.*
import org.jetbrains.uast.*


fun PsiElement.resolveAllParameterAnnotations(externalAnnotationsManager: ExternalAnnotationsManager? = null): List<List<UAnnotation>> {
    val extManager = externalAnnotationsManager ?: ExternalAnnotationsManager.getInstance(project)
    return when (this) {
        is KtParameter -> {
            if (this.typeReference?.isFunctional() == true) {
                val resolvedType = this.resolveType2() ?: return emptyList()
                val params = resolvedType.resolveFunctionInputValueParameters()
                return params.map { it.resolveAnnotation() }
            } else {
                //resolve function annotations?
                emptyList()
            }
        }

        is KtLightMethod -> this.reference?.resolve()?.resolveAllParameterAnnotations(externalAnnotationsManager)
            ?: emptyList()

        is KtFunction -> valueParameters.getAllAnnotations(extManager)
        is PsiMethod -> parameterList.getAllAnnotations(extManager)
        else -> emptyList()
    }
}


fun KotlinType.resolveFunctionInputValueParameters(): List<TypeProjection> {
    if (arguments.size <= 1) {
        return emptyList()
    }
    return arguments.take(arguments.size - 1)
}

fun TypeProjection.resolveAnnotation(): List<UAnnotation> {
    return this.type.annotations.mapNotNull {
        val annotationDescription = it as? LazyAnnotationDescriptor
        annotationDescription?.annotationEntry.toUElement(UAnnotation::class.java)
    }
}

fun KtFunction.numberParametersWithDefaultValue(): List<KtParameter> {
    return valueParameters.filter {
        it.isNumberTypeWithDefaultValue()
    }
}

fun KtParameter.isNumberTypeWithDefaultValue(): Boolean {
    return isNumberType() &&
            hasDefaultValue()
}

fun KtParameter.isNumberType(): Boolean {
    return resolveType2()?.isPrimitiveNumberOrNullableType() == true
}

fun KtDeclaration.resolveType2(): KotlinType? =
    (resolveToDescriptorIfAny() as? CallableDescriptor)?.returnType


fun KtFunction.parametersWithAnnotations(
    filter: (KtParameter, List<UAnnotation>) -> Boolean = { _, _ -> true }
): List<Pair<KtParameter, List<UAnnotation>>> {
    val result: MutableList<Pair<KtParameter, List<UAnnotation>>> = mutableListOf()
    val allAnnotations = resolveAllParameterAnnotations()
    valueParameters.forEachIndexed { index, ktParameter ->
        val annotations = allAnnotations.getOrNull(index) ?: emptyList()
        result.addIf(
            filter(ktParameter, annotations),
            Pair(ktParameter, annotations)
        )
    }
    return result
}

fun KtFunction.parametersFromCallWithAnnotations(
    invocationSite: KtCallExpression,
    filter: (ParameterToValueExpression) -> Boolean = { _ -> true }
): List<ParameterToValueExpression> {
    val result: MutableList<ParameterToValueExpression> = mutableListOf()

    val parametersToCallValues: LinkedHashMap<String, ParameterToValueExpression> =
        resolveValueParametersTo(invocationSite)

    parametersToCallValues.forEachIndexed { (_: String, parameterToValueExpression: ParameterToValueExpression), index ->
        result.addIf(
            condition = filter(parameterToValueExpression),
            item = parameterToValueExpression
        )
    }

    return result
}

fun KtFunction.resolveValueParametersTo(
    callExpression: KtCallExpression
): LinkedHashMap<String, ParameterToValueExpression> {
    val result = LinkedHashMap<String, MutableParameterToValueExpression>()
    fillValueParametersInto(into = result)
    callExpression.fillValueArgumentsInto(into = result)
    return result as LinkedHashMap<String, ParameterToValueExpression>
}

fun KtFunction.fillValueParametersInto(
    into: MutableMap<String, MutableParameterToValueExpression>
) {
    valueParameters.forEach { parameter ->
        val name = parameter.name ?: return@forEach //TODO when /  how can this happen??
        val annotations = parameter.annotationEntries
        into[name] = MutableParameterToValueExpression(
            parameter = parameter,
            valueArgument = parameter.defaultValue,
            parameterAnnotations = annotations
        )
    }
}

//@SideEffects
fun KtCallExpression.fillValueArgumentsInto(into: Map<String, MutableParameterToValueExpression>) {
    //the rules of argument order can be described as such:
    //-if named then that precedes all (allows to mix'n match all orders)
    //otherwise it must be that up-to eventual all named is index based (you cannot have: a default,  a named followed by some without name)
    // You can have correct position and add names to some (as long as it is positionally correct)
    //- thus position is secondary if not named.
    //- default arguments are thus "last" in the order (either not set, or overwritten)
    valueArguments.forEachIndexed { index, argument ->
        val argName = argument.getArgumentName()?.text
        if (argName != null) {
            into[argName]?.valueArgument = argument.getArgumentExpression()
            return@forEachIndexed
        }
        val parameter = into.entries.getOrNull(index)
        if (parameter == null) {
            TODO("log error")
        }
        parameter.value.valueArgument = argument.getArgumentExpression()
    }
}

sealed interface ParameterToValueExpression {
    val parameter: KtParameter
    val valueArgument: KtExpression?
    val parameterAnnotations: List<KtAnnotationEntry>
}

fun ParameterToValueExpression(
    parameter: KtParameter,
    valueArgument: KtExpression?,
    annotations: List<KtAnnotationEntry>
): ParameterToValueExpression = MutableParameterToValueExpression(
    parameter = parameter,
    valueArgument = valueArgument,
    parameterAnnotations = annotations
)

data class MutableParameterToValueExpression(
    override var parameter: KtParameter,
    override var valueArgument: KtExpression?,
    override var parameterAnnotations: List<KtAnnotationEntry>
) : ParameterToValueExpression


class KtCallExpressionFindValueArgumentBy {
    //@Test
    fun notThere() {
        @Language("kotlin")
        val code = """
            fun x(){ }   
            fun useMissing(){
                x(42) //ERROR parameter should not be found
            }
        """.trimIndent()
    }

    //@Test
    fun there() {
        @Language("kotlin")
        val code = """
            fun x(a: Int){ }   
            fun useX(){
                x(42) //Valid, should give a, 42, []
            }
        """.trimIndent()
    }

    //@Test
    fun thereWithAnnotations() {
        @Language("kotlin")
        val code = """
            fun x(@IntLimit(from = 0) a: Int){ }   
            fun useX(){
                x(42) //Valid, should give a, 42, [IntLimit]
            }
        """.trimIndent()
    }

    //@Test
    fun thereWithDefaultArg() {
        @Language("kotlin")
        val code = """
            fun x(a: Int= 42){ }   
            fun useX(){
                x() //Valid, should give a, 42, []
                x(50) //Valid, should give a, 50, []
            }
        """.trimIndent()
    }

    //@Test
    fun thereWithDefaultArgAndAnnotation() {
        @Language("kotlin")
        val code = """
            fun x(@IntLimit(from = 0) a: Int= 42){ }   
            fun useX(){
                x() //Valid, should give a, 42, [IntLimit]
                x(50) //Valid, should give a, 50, [IntLimit]
            }
        """.trimIndent()
    }

    //@Test
    fun thereSecond() {
        @Language("kotlin")
        val code = """
            fun x(y: Int,  @IntLimit(from = 0) a: Int){ }   
            fun useX(){
                x(0,42) //Valid, should give a, 42, [IntLimit]
            }
        """.trimIndent()
    }

    //@Test
    fun thereSecondAllDefault() {
        @Language("kotlin")
        val code = """
            fun x(y: Int = 22,  @IntLimit(from = 0) a: Int= 12){ }   
            fun useX(){
                x(80) //Valid, should give a, 12, [IntLimit]
                x(100) //Valid, should give a, 12, [IntLimit]
            }
        """.trimIndent()
    }

    //@Test
    fun thereSecondAllDefaultNamedArgs() {
        @Language("kotlin")
        val code = """
            fun x(y: Int = 22,  @IntLimit(from = 0) a: Int= 10){ }   
            fun useX(){
                x(a = 42) //Valid, should give a, 42, [IntLimit]
                x(a = 50) //Valid, should give a, 42, [IntLimit]
            }
        """.trimIndent()
    }
}


//fun KtCallElement.getArgumentByParameterIndex(index: Int, context: BindingContext): List {
//    val resolvedCall = getResolvedCall(context) ?: return emptyList()
//    val parameterToProcess = resolvedCall.resultingDescriptor.valueParameters.getOrNull(index) ?: return emptyList()
//    return resolvedCall.valueArguments[parameterToProcess]?.arguments ?: emptyList()