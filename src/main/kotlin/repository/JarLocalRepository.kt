package org.kechinvv.repository

import java.nio.file.Path

class JarLocalRepository(val targetJar: Path, path: Path) : AbstractLocalRepository(path) {

    override fun getPathForClassFiles(): List<Path> {
        return listOf(targetJar)
    }

    override fun getPathForJarFiles(): List<Path> {
        return listOf(targetJar)
    }
}