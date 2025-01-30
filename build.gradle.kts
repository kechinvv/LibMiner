plugins {
    kotlin("jvm") version "2.0.20"
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
    //todo: remove unnecessary
    implementation("org.soot-oss:sootup.core:1.3.0")
    implementation("org.soot-oss:sootup.java.core:1.3.0")
    implementation("org.soot-oss:sootup.java.sourcecode:1.3.0")
    implementation("org.soot-oss:sootup.java.bytecode:1.3.0")
    implementation("org.soot-oss:sootup.jimple.parser:1.3.0")
    implementation("org.soot-oss:sootup.callgraph:1.3.0")
    implementation("org.soot-oss:sootup.analysis:1.3.0")
    implementation("org.soot-oss:sootup.qilin:1.3.0")

    implementation("org.gradle:gradle-tooling-api:8.9")

    implementation("org.apache.maven.shared:maven-verifier:2.0.0-M1")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.1.0.202411261347-r")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}