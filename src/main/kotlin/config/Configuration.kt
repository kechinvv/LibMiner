package org.kechinvv.config

import kotlinx.serialization.Serializable
import java.nio.file.Path
import java.nio.file.Paths

@Serializable
class Configuration {
    var projectsConfiguration: ProjectsConfiguration = ProjectsConfiguration()
    var fsmConfiguration: FsmConfiguration = FsmConfiguration()

    // qualified package or class
    var targetLibExtractingUnit: Set<String> = setOf("")


    // 0 - limitless
    var countOfProjects: Int = 1000

    var workdir: Path = Paths.get(".")
    var sourceDB: Path = workdir.resolve("db.db")
    var cacheSize: Int = 10000

    // fuzzing limits
    var fuzzingExecutions: Int = 10000
    var fuzzingTimeInSeconds: Int = 300

    // static extracting
    var traversLength: Int = 1000
    var traversDepth: Int = 6
    var traceCount: Int = 1000000
}