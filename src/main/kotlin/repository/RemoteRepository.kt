package org.kechinvv.repository

import org.kechinvv.utils.PrjSource
import java.nio.file.Path

interface RemoteRepository {
    val repositoryData: RepositoryData

    fun cloneTo(outputDir: Path): AbstractLocalRepository

    fun getSourceType(): PrjSource
}