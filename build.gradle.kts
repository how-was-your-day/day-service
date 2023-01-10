import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version : String by project
val kotlin_version : String by project
val logback_version : String by project
val mongo_version : String by project
val coroutines_version : String by project
val kafkaVersion : String by project

plugins {
    kotlin("jvm") version "1.7.20"
    id("io.ktor.plugin") version "2.2.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.20"
}

group = "brown.jorge"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")

    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")
    implementation("io.ktor:ktor:$ktor_version")
    implementation("io.ktor:ktor-io:$ktor_version")
    implementation("io.ktor:ktor-server:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-http:$ktor_version")
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")

    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("org.mongodb:mongodb-driver-sync:$mongo_version")

    implementation("com.typesafe:config:1.4.2")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}