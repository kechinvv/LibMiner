package org.kechinvv.repository

import org.gradle.tooling.GradleConnector
import java.io.File
import java.nio.file.Path

class GradleWorker(override val path: Path, gradlePath: String?, gradleVersion: String?) : BuildSystem {

    private val connector = GradleConnector.newConnector()

    init {
        if (gradlePath != null) {
            connector.useInstallation(File(gradlePath))
        } else if (gradleVersion != null) {
            connector.useGradleVersion(gradleVersion)
        }
        connector.forProjectDirectory(path.toFile())
    }

    override fun build() {
        connector.connect().use {
            val build = it.newBuild()
            build.forTasks("clean")
            build.run()
            build.forTasks("build").withArguments("-x", "test")
            build.run()
        }
    }

    override fun runTest() {
        connector.connect().use {
            val test = it.newBuild()
            test.forTasks("cleanTest", "test")
                .withArguments("-i")
                .withArguments("-x", "build")
                .withArguments("-x", "compileJava")


            test.setStandardOutput(System.out)
            test.setStandardError(System.out)
            test.run()
        }
    }
}