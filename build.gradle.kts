plugins {
    id("org.jetbrains.intellij") version "0.4.21"
    kotlin("jvm") version "1.3.72"
    java
    id("org.owasp.dependencycheck") version "5.3.2"
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
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.36")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.18")
    implementation("csense.kotlin:csense-kotlin-ds-jvm:0.0.25")
    implementation("csense.idea.base:csense-idea-base:0.1.19")
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
