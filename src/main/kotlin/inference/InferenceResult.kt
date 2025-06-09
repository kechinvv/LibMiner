package org.kechinvv.inference

import java.nio.file.Path

data class InferenceResult(val mintDot: Path, val unionDot: Path?, val json: Path?)
