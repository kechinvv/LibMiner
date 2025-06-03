package org.kechinvv.config

import kotlinx.serialization.Serializable

@Serializable
class Configuration {
    var ghQuerySearch: String = ""
    var ghLanguageSearch: String = ""
    var ghToken: String = ""
    var onlyWithJars: Boolean = false
    var useGh: Boolean = true

    var targetLibGroup: String = ""
    var targetLibName: String = ""
    var targetLibVersion: String = ""
    var useMvn: Boolean = true

    // qualified package or class
    var targetLibExtractingUnit: String = ""

    var mavenPathOrEnvVar: String = "mvn"

    // Auto get necessary gradle for every project if null or
    // Installed gradle (optional)
    var gradlePath: String? = null

    // Version for downloaded gradle (optional)
    var gradleVersion: String? = null

    // 0 - limitless
    var countOfProjects: Int = 0

    var workdir: String = "."
    var sourceDB: String? = null

    // fuzzing limits
    var fuzzingExecutions: Int = 100
    var fuzzingTimeInSeconds: Int = 100

    // static extracting
    var traversLength: Int = 1000
    var traversDepth: Int = 6
    var traceCount: Int = 1000000

    // inference
    var useSignature: Boolean = false
    var unionEnd: Boolean = true
    var toJson: Boolean = true

    // k for merging
    var kTail: Int = 2
}