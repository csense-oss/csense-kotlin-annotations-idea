package csense.kotlin.annotations.inheritance

import kotlin.reflect.*

@Target(
    AnnotationTarget.CLASS //for functional types
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class RequiresAnnotation(
    val annotationClass: KClass<out Annotation>
)

annotation class ClassHierarchy

<error descr="NOT ANNOTATED WITH \"csense.kotlin.annotations.inheritance.ClassHierarchy\" which is required">@RequiresAnnotation(ClassHierarchy::class)
open class MyParent2</error>

<error descr="NOT ANNOTATED WITH \"csense.kotlin.annotations.inheritance.ClassHierarchy\" which is required">class MyChild2: MyParent2()</error>