plugins {
    kotlin("jvm") version "2.3.20" apply false
    id("org.jetbrains.compose") version "1.10.3" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20" apply false
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
