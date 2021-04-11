import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0-M2"
    application
}

group = "me.lobis"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.kotlin.link")
    maven("https://dl.bintray.com/pdvrieze/maven") // could be replaced by jcenter()
}
dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("space.kscience:gdml:0.4.0-dev-8") // old dev-3
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClassName = "MainKt"
}