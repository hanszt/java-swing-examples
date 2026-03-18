plugins {
    kotlin("jvm") version "2.3.10"
    application
}

group   = "com.stockviewer"
version = "1.0.0"

val jacksonVersion = "2.21.1"
val logbackVersion = "1.5.32"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.core:jackson-core:${jacksonVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
}

application {
    mainClass.set("com.stockviewer.StockViewerKt")
}

kotlin {
    jvmToolchain(25)
}

// Fat-jar task so the app can be run with  java -jar stockviewer.jar
tasks.jar {
    archiveBaseName.set("stockviewer")
    manifest { attributes["Main-Class"] = "com.stockviewer.StockViewerKt" }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
