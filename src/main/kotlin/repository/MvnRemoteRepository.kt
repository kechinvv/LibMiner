package org.kechinvv.repository

import org.gradle.tooling.GradleConnector
import org.kechinvv.MvnLibDownloadFailed
import org.kechinvv.utils.logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Logger
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

val TEMP_DIR = Paths.get("./temp-gradle-project").createDirectories().toFile()

data class MvnRemoteRepository(val group: String, val name: String, val version: String) : RemoteRepository {
    companion object {
        val LOG by logger()
    }

    override fun cloneTo(outputDir: Path): AbstractLocalRepository {

        GradleConnector.newConnector()
            .forProjectDirectory(TEMP_DIR)
            .connect().use { connection ->
                connection.newBuild()
                    .forTasks("copyLibsToDir")
                    .withArguments(
                        "-Plibdir=$outputDir",
                        "-Plibname=$name",
                        "-Plibversion=$version",
                        "-Plibgroup=$group"
                    )
                    .run()
            }
        if (outputDir.notExists()) throw MvnLibDownloadFailed()
        return JarLocalRepository("$name-$version.jar", outputDir.toFile())
    }

}