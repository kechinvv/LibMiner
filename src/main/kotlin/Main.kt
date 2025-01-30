package org.kechinvv

import org.kechinvv.repository.RemoteLib
import java.nio.file.Paths
import kotlin.io.path.createDirectory

fun main() {
    val target = Paths.get("./downloaded_reps")
    target.createDirectory()
    RemoteLib("com.atlassian.bamboo", "bamboo-specs-runner", "10.2.1").cloneTo(target)
}