plugins {
    id("org.jetbrains.intellij") version "0.4.10"
    kotlin("jvm") version "1.3.60"
    java
    id("org.owasp.dependencycheck") version "5.1.0"
}

group = "csense.kotlin"
version = "0.1"


// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    updateSinceUntilBuild = false //Disables updating since-build attribute in plugin.xml
    setPlugins("kotlin", "java")
    version = "2019.2"
}

repositories {
    jcenter()
    //until ds is in jcenter
    maven(url = "https://dl.bintray.com/csense-oss/maven")
}

dependencies {
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.25")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.7")
    implementation("csense.kotlin:csense-kotlin-ds-jvm:0.0.24")
}

tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      This is currently a very early release, and so do not expect it to be perfect.
       
      Currently it supports the following
      <ul>
        <li>Android runOnUiThread thread construct</li>
        <li>Android annotations for ranges and threading (both support and android x libs)</li>
        <li>Externally able to read annotations for some things (still needs improvement though)</li>
        <li>Some Coroutines constructions are understood / analyzed (launch / async, withContext)</li>
        <li>(SWING/AWT)SwingUtilities.invokeLater,(SWT)Display.syncExec,(JAVAFX) Platform.runLater,(ANDROID)android.view.View.post* are understood in changes from background to ui</li>
        <li>Ranges (from csense, jetbrains,android) are parsed  analyzed(still needs improvement though)</li>
        <li>Makes csense annotations available externally.(go to declaration in the library and then add annotation)</li>
      </ul>
      
      """)
}

tasks.getByName("check").dependsOn("dependencyCheckAnalyze")

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
    this.kotlinOptions.jvmTarget = "1.8"
}
