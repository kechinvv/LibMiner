package org.kechinvv.repository

import org.kechinvv.config.Configuration
import java.nio.file.Path

class MavenWorker(override val path: Path, override val configuration: Configuration) : BuildSystem {

    override fun build() {
        TODO("Not yet implemented")
    }

    override fun runTest() {
        TODO("Not yet implemented")
    }
}