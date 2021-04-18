plugins {
    //https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "0.7.2"
    kotlin("jvm") version "1.4.32"
    java
    //https://github.com/jeremylong/dependency-check-gradle/releases
    id("org.owasp.dependencycheck") version "6.1.4"
}

group = "csense.kotlin"
version = "0.7.0"


// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    updateSinceUntilBuild = false //Disables updating since-build attribute in plugin.xml
    setPlugins("Kotlin", "java") // "java" if target 192 and above in plugin.xml
    version = "2020.1"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://pkgs.dev.azure.com/csense-oss/csense-oss/_packaging/csense-oss/maven/v1")
        name = "csense-oss"
    }
}

dependencies {
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.46")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.41")
    implementation("csense.kotlin:csense-kotlin-datastructures-algorithms:0.0.41")
    implementation("csense.idea.base:csense-idea-base:0.1.22")
}

tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes(
        """
      <ul>
        <li>now understands number annotations on types (which helps for functional declarations)</li>
        <li>now inspects default values with regards to number limits / ranges</li>
        <li>handles nullable numbers for number limits / ranges</li>
        <li>fixed to android and regular number ranges </li>
        <li>Inspects default values for variables for number limits / ranges</li>
        <li>Performance for thread analyzers improved drastically</li>
        <li>Fixes for threading analyzers</li>
      </ul>
      """
    )
}

tasks.getByName("check").dependsOn("dependencyCheckAnalyze")

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
    this.kotlinOptions.jvmTarget = "1.8"
}
