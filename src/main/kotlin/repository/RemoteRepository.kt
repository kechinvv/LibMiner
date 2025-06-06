package org.kechinvv.repository

import java.nio.file.Path

interface RemoteRepository {
    val url: String
    val name: String

    fun cloneTo(outputDir: Path): AbstractLocalRepository
}