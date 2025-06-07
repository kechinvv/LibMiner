package org.kechinvv.repository

import org.kechinvv.config.Configuration
import java.nio.file.Path

interface BuildSystem {
    val path: Path
    val configuration: Configuration

    fun build()
    fun runTest()

}