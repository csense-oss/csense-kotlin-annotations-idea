plugins {
    //https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "0.6.5"
    kotlin("jvm") version "1.4.21"
    java
    //https://github.com/jeremylong/dependency-check-gradle/releases
    id("org.owasp.dependencycheck") version "6.0.4"
}

group = "csense.kotlin"
version = "0.7.0"


// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    updateSinceUntilBuild = false //Disables updating since-build attribute in plugin.xml
    setPlugins("Kotlin", "java") // "java" if target 192 and above in plugin.xml
    version = "2019.2"
    alternativeIdePath =  "C:\\Users\\kasper\\AppData\\Local\\JetBrains\\Toolbox\\apps\\AndroidStudio\\ch-0\\193.6514223"
}

repositories {
    jcenter()
    //until ds is in jcenter
    maven(url = "https://dl.bintray.com/csense-oss/maven")
    maven(url = "https://dl.bintray.com/csense-oss/idea")
}

dependencies {
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.45")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.40")
    implementation("csense.kotlin:csense-kotlin-datastructures-algorithms:0.0.41")
    implementation("csense.idea.base:csense-idea-base:0.1.20")
}

tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      <ul>
        <li>now understands number annotations on types (which helps for functional declarations)</li>
        <li>Performance for thread analyzers improved drastically</li>
        <li>Fixes for threading analyzers</li>
      </ul>
      """)
}

tasks.getByName("check").dependsOn("dependencyCheckAnalyze")

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
    this.kotlinOptions.jvmTarget = "1.8"
}
