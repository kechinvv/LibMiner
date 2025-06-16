package org.kechinvv.config

import kotlinx.serialization.Serializable

@Serializable
class ProjectsConfiguration {
    var ghToken: String = if (System.getenv("GH_TOKEN") == null) "" else System.getenv("GH_TOKEN")

    var ghQuerySearch: String = ""
    var ghLanguageSearch: String? = "java"
    var ghFileName: String? = null
    var onlyWithJars: Boolean = false
    var useGh: Boolean = true

    var targetLibGroup: String = ""
    var targetLibName: String = ""
    var targetLibVersion: String = ""
    var useMvn: Boolean = true

    var mavenPathOrEnvVar: String = "mvn"

    // Auto get necessary gradle for every project if null or
    // Installed gradle (optional)
    var gradlePath: String? = null

    // Version for downloaded gradle (optional)
    var gradleVersion: String? = null
}