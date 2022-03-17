@file:Suppress("unused")

package csense.kotlin.annotations.inheritance

import kotlin.reflect.*

@Target(
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class RequiresAnnotation(
    val annotationClass: KClass<out Annotation>
)

annotation class Serializable

interface X<T>{

    fun fetchFromNetwork(@RequiresAnnotation(Serializable::class) x: T)
   
}


data class Project(val name: String, val language: String)

<error descr="NOT ANNOTATED WITH \"csense.kotlin.annotations.inheritance.Serializable\" which is required (by \"fetchFromNetwork\")">class Y: X<Project></error>