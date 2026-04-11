import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

val logbackVersion = "1.5.32"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":stockviewer-shared"))
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
}

application {
    mainClass.set("com.stockviewer.ui.swing.StockViewerSwing")
}

tasks.withType<JavaExec>().named("run") {
    group = "application"
    description = "Runs the Swing application"
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

tasks.jar {
    archiveBaseName.set("stockviewer-swing")
    manifest { attributes["Main-Class"] = "com.stockviewer.ui.swing.StockViewerSwingKt" }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
