plugins {
    kotlin("jvm") version "2.3.20"
    id("org.jetbrains.compose") version "1.10.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20"
    application
}

group = "com.stockviewer"
version = "1.0.0"

val jacksonVersion = "2.21.1"
val logbackVersion = "1.5.32"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
}

application {
    mainClass.set("com.stockviewer.ui.compose.StockViewerCompose")
}

kotlin {
    jvmToolchain(25)
}

// Rename the conflicting task
tasks.getByName<JavaExec>("run") {
    group = "runSwing"
}

// Fat-jar task so the app can be run with  java -jar stockviewer.jar
tasks.jar {
    archiveBaseName.set("stockviewercompose")
    manifest { attributes["Main-Class"] = "com.stockviewer.ui.compose.StockViewerCompose" }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
