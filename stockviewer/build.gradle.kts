plugins {
    kotlin("jvm") version "2.3.10"
    application
}

group   = "com.stockviewer"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
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
