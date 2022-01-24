plugins {
    //https://plugins.gradle.org/plugin/org.jetbrains.intellij
    id("org.jetbrains.intellij") version "1.3.1"
    kotlin("jvm") version "1.6.10"
    java
    //https://github.com/jeremylong/DependencyCheck
    id("org.owasp.dependencycheck") version "6.5.3"
}


group = "csense.kotlin"
version = "0.8.0"


// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    updateSinceUntilBuild.set(false)
    plugins.set(listOf("Kotlin", "java"))
    version.set("2020.3")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://pkgs.dev.azure.com/csense-oss/csense-oss/_packaging/csense-oss/maven/v1")
        name = "csense-oss"
    }
}

dependencies {
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.54")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.50")
    implementation("csense.kotlin:csense-kotlin-datastructures-algorithms:0.0.41")
    implementation("csense.idea.base:csense-idea-base:0.1.41")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
    testImplementation("csense.kotlin:csense-kotlin-tests:0.0.53")
    testImplementation("csense.idea.test:csense-idea-test:0.1.0")
}


tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes.set(
        """
      <ul>
        <li>Works with newer idea & android studios</li>
      </ul>
      """
    )
}

tasks.getByName("check").dependsOn("dependencyCheckAnalyze")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-progressive")
}

tasks.withType<JavaCompile> {
    targetCompatibility = "1.8"
    sourceCompatibility = "1.8"
}