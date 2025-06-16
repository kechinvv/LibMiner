package org.kechinvv.repository

import org.kechinvv.config.ProjectsConfiguration
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

class LocalRepository(path: Path, private val configuration: ProjectsConfiguration) : AbstractLocalRepository(path) {

    private val targets: HashSet<BuildSystem> = HashSet()

    init {
        Files.walk(path).forEach {
            if (it.name == "build.gradle" || it.name == "build.gradle.kts") targets.add(
                GradleWorker(
                    it.parent,
                    configuration.gradlePath,
                    configuration.gradleVersion
                )
            )
            else if (it.name == "pom.xml") targets.add(MavenWorker(it.parent, configuration.mavenPathOrEnvVar))
        }
    }


    fun build() {
        targets.forEach(BuildSystem::build)
    }

    fun runTests() {
        targets.forEach(BuildSystem::runTest)
    }

    override fun getPathForClassFiles(): List<Path> {
        return targets.map { it.getClassFolderPath() }
    }

    override fun getPathForJarFiles(): List<Path> {
        return this.getJars().toList()
    }
}