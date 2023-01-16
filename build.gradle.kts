plugins {
    //https://plugins.gradle.org/plugin/org.jetbrains.intellij
    id("org.jetbrains.intellij") version "1.12.0"
    kotlin("jvm") version "1.8.0"
    //https://github.com/jeremylong/DependencyCheck
    id("org.owasp.dependencycheck") version "7.4.4"
}


group = "csense.kotlin"
version = "0.9.0"


// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    updateSinceUntilBuild.set(false)
    plugins.set(listOf("Kotlin", "java"))
    version.set("2021.3")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://pkgs.dev.azure.com/csense-oss/csense-oss/_packaging/csense-oss/maven/v1")
        name = "csense-oss"
    }
}

dependencies {
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.59")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.41")
    implementation("csense.kotlin:csense-kotlin-datastructures-algorithms:0.0.41")
    implementation("csense.idea.base:csense-idea-base:0.1.60")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("csense.kotlin:csense-kotlin-tests:0.0.59")
    testImplementation("csense.idea.test:csense-idea-test:0.2.0")
}


tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes.set(
        """
      <ul>
        <li>Fixed weird issue (index out of bounds)</li>
        <li>Fixed bugs with different things (eg bad type resolution etc)</li>
      </ul>
      """
    )
}

tasks.getByName("check").dependsOn("dependencyCheckAnalyze")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs = listOf("-progressive","-opt-in=kotlin.contracts.ExperimentalContracts")
}

tasks.withType<JavaCompile> {
    targetCompatibility = "11"
    sourceCompatibility = "11"
}