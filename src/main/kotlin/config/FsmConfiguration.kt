package org.kechinvv.config

import kotlinx.serialization.Serializable
import java.nio.file.Path
import java.nio.file.Paths

@Serializable
class FsmConfiguration {
    var mintFilesPath: Path = Paths.get("./mintworkdir")
    val jsonAndDotFilesPath: Path = mintFilesPath

    // inference
    var useSignature: Boolean = false
    var unionEnd: Boolean = true
    var toJson: Boolean = true

    var absoluteFilter: Int? = 10
    var relativeFilter: Int? = null

    // k for merging
    var kTail: Int = 2
    var strategy: String = "ktails"
}