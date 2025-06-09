package org.kechinvv.repository

import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class MavenWorker(override val path: Path, val mavenPathOrEnv: String) : BuildSystem {

    override fun build() {
        val mvnBuilder = ProcessBuilder(mavenPathOrEnv)
        mvnBuilder.command("-DskipTests=true", "-f", "${path.toAbsolutePath()}${File.separator}pom.xml", "package")
        val ps = mvnBuilder.start()
        ps.waitFor(60 * 2, TimeUnit.SECONDS)
        ps.destroyForcibly()
    }

    override fun runTest() {
        val runTest = ProcessBuilder(mavenPathOrEnv)
        runTest.command("-Dmaven.main.skip", "-f", "${path.toAbsolutePath()}${File.separator}pom.xml", "test")
        val ps = runTest.start()
        ps.waitFor(60 * 3, TimeUnit.SECONDS)
        ps.destroyForcibly()
    }
}