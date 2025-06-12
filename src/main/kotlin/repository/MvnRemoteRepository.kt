package org.kechinvv.repository

import org.gradle.tooling.GradleConnector
import org.kechinvv.MvnLibDownloadFailed
import org.kechinvv.utils.PrjSource
import org.kechinvv.utils.logger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.notExists

val TEMP_DIR = Paths.get("./temp-gradle-project").createDirectories().toFile()

data class MvnRemoteRepository(
    override val repositoryData: RepositoryData
) : RemoteRepository {

    constructor(
        name: String,
        group: String,
        version: String,
    ) : this(RepositoryData(name, "pkg:maven/$group/$name@$version", group, version))

    override fun cloneTo(outputDir: Path): AbstractLocalRepository {
        val absoluteOutputDir = outputDir.toAbsolutePath()
        GradleConnector.newConnector()
            .forProjectDirectory(TEMP_DIR)
            .connect().use { connection ->
                connection.newBuild()
                    .forTasks("copyLibsToDir")
                    .withArguments(
                        "-Plibdir=$absoluteOutputDir",
                        "-Plibname=${repositoryData.name}",
                        "-Plibversion=${repositoryData.version}",
                        "-Plibgroup=${repositoryData.group}"
                    )
                    .run()
            }
        if (outputDir.notExists()) throw MvnLibDownloadFailed()
        val targetJarPath =
            Files.walk(outputDir).filter {
                !it.isDirectory() && it.name.contains(repositoryData.name) && it.name.contains(repositoryData.version!!)
            }.findFirst().orElseThrow()
        return JarLocalRepository(targetJarPath, outputDir)
    }

    override fun getSourceType(): PrjSource = PrjSource.MAVEN_CENTRAL

}