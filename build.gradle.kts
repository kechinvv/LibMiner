plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
}

group = "org.kechinvv"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
    google()
    maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.2")


    implementation("org.apache.commons:commons-compress:1.26.0")

    implementation("org.gradle:gradle-tooling-api:8.9")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("com.charleskorn.kaml:kaml:0.74.0")

    implementation("org.apache.maven.shared:maven-verifier:2.0.0-M1")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.1.0.202411261347-r")
    implementation("net.lingala.zip4j:zip4j:2.11.5") //todo: rewrite usages and remove


    implementation("org.soot-oss:soot:4.6.0")

    implementation("org.ktorm:ktorm-core:4.1.1")
    implementation("org.ktorm:ktorm-jackson:4.1.1")
    implementation("org.ktorm:ktorm-support-sqlite:4.1.1")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")

    implementation(files("libs/mint-core-1.0.0-jar-with-dependencies.jar"))
    implementation(files("libs/mint-inference-1.2.0-jar-with-dependencies.jar"))
    implementation(files("libs/mint-testgen-1.1.0-jar-with-dependencies.jar"))

    implementation("guru.nidi:graphviz-java-all-j2v8:0.18.1")

    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

}

kotlin {
    jvmToolchain(21)
}

