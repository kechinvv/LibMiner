plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
}

group = "org.kechinvv"
version = "1.0-SNAPSHOT"

val aetherVersion = "1.1.0"
val mavenVersion = "3.3.9"

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.gradle:gradle-tooling-api:8.9")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("com.charleskorn.kaml:kaml:0.74.0")

    implementation("org.apache.maven.shared:maven-verifier:2.0.0-M1")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.1.0.202411261347-r")
    implementation("net.lingala.zip4j:zip4j:2.11.5")

    implementation("org.soot-oss:soot:4.6.0")

    implementation("org.ktorm:ktorm-core:4.1.1")
    implementation("org.ktorm:ktorm-jackson:4.1.1")
    implementation("org.ktorm:ktorm-support-sqlite:4.1.1")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")

    implementation(files("libs/mint-core-1.0.0-jar-with-dependencies.jar"))
    implementation(files("libs/mint-inference-1.2.0-jar-with-dependencies.jar"))
    implementation(files("libs/mint-testgen-1.1.0-jar-with-dependencies.jar"))

    implementation("guru.nidi:graphviz-java-all-j2v8:0.18.1")
    implementation("org.apache.logging.log4j:log4j-bom:2.24.3")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}