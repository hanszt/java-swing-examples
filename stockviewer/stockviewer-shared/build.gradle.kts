import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.jackson.core)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.coroutines)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.get()))
    }
}
