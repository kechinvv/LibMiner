package org.kechinvv.repository

import org.gradle.tooling.GradleConnector
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Logger
import kotlin.io.path.notExists

val GRADLE_TEMPLATE = Any::class::class.java.getResource("/build.gradle")!!.readText(Charsets.UTF_8)
val TEMP_DIR: Path = Paths.get("temp-gradle-project")

data class RemoteLib(val group: String, val name: String, val version: String) : RemoteRepository {
    val LOG = Logger.getLogger(this.javaClass.name)

    override fun cloneTo(outputDir: Path): LocalRepository? {
        TEMP_DIR.toFile().mkdirs();

        val buildScript = String.format(
            GRADLE_TEMPLATE,
            group, name, version
        )

        val buildFile = TEMP_DIR.resolve("build.gradle")
        val setFile = TEMP_DIR.resolve("settings.gradle")
        Files.write(buildFile, buildScript.toByteArray())
        if (setFile.notExists()) {
            Files.createFile(setFile)
        }

        GradleConnector.newConnector()
            .forProjectDirectory(TEMP_DIR.toFile())
            .connect().use { connection ->
                connection.newBuild()
                    .forTasks("downloadJars")
                    .run()

                val gradleHome = File(System.getProperty("user.home"), ".gradle")
                val cacheDir = File(gradleHome, "caches/modules-2/files-2.1/$group/$name/$version")
                if (cacheDir.exists() && cacheDir.isDirectory) {
                    //todo: mb filter jar from sources, mb try to get all possible data (jar+pom+sources)
                    val files = cacheDir.walk().filter { it.isFile && it.extension == "jar" }.toList()
                    if (files.isNotEmpty()) {
                        val resDir = outputDir.resolve(group + "_" + name + "_" + version)
                        for (file in files) {
                            LOG.info("Resolved artifact: " + file.absolutePath)
                            file.copyTo(resDir.resolve(file.name).toFile())
                        }
                        return JarLocalRepository(resDir.toFile())
                    }
                    return null
                }
                LOG.info("Artifact not found in Gradle cache.")
                return null

            }
    }
}