import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.compose")
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    application
}

val logbackVersion = "1.5.32"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(project(":stockviewer-shared"))
    implementation(compose.desktop.currentOs)
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
}

application {
    mainClass.set("com.stockviewer.ui.compose.StockViewerCompose")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

tasks.jar {
    archiveBaseName.set("stockviewer-compose")
    manifest { attributes["Main-Class"] = "com.stockviewer.ui.compose.StockViewerCompose" }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
