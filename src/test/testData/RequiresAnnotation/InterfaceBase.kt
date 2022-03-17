package csense.kotlin.annotations.inheritance

import kotlin.reflect.*

@Target(
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class RequiresAnnotation(
    val annotationClass: KClass<out Annotation>
)

annotation class ClassHierarchy

@RequiresAnnotation(ClassHierarchy::class)
interface MyParent


<error descr="NOT ANNOTATED WITH \"csense.kotlin.annotations.inheritance.ClassHierarchy\" which is required">class Impl : MyParent</error>