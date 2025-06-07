package org.kechinvv.repository

import org.gradle.tooling.GradleConnector
import org.kechinvv.config.Configuration
import java.io.File
import java.nio.file.Path

class GradleWorker(override val path: Path, override val configuration: Configuration) : BuildSystem {

    private val connector = GradleConnector.newConnector()

    init {
        if (configuration.gradlePath != null) {
            connector.useInstallation(File(configuration.gradlePath!!))
        } else if (configuration.gradleVersion != null) {
            connector.useGradleVersion(configuration.gradleVersion)
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