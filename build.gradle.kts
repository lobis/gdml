import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.31"
    application
}

group = "me.luis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.kotlin.link")
    maven("https://dl.bintray.com/pdvrieze/maven") // could be replaced by jcenter()
}
dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("space.kscience:gdml:0.4.0-dev-1")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "15"
}

application {
    mainClassName = "MainKt"
}