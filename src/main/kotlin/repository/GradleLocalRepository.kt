package org.kechinvv.repository

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressEvent
import org.gradle.tooling.ProgressListener
import org.kechinvv.config.Configuration
import java.io.File

class GradleLocalRepository(file: File, private val configuration: Configuration) : LocalRepository(file) {
    private val connector = GradleConnector.newConnector()

    init {
        //connector.useInstallation(File(configuration.gradlePath))
        connector.forProjectDirectory(file)
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

    override fun runTests() {
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