package org.kechinvv.repository

import java.nio.file.Path

interface BuildSystem {
    val path: Path

    fun build()
    fun runTest()

}