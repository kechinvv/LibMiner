package org.kechinvv.repository

import java.nio.file.Path

interface RemoteRepository {

    fun cloneTo(outputDir: Path): AbstractLocalRepository?
}