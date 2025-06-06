package org.kechinvv.repository

import java.io.File
import java.nio.file.Path

class JarLocalRepository(val targetJar: Path, file: File) : AbstractLocalRepository(file) {


}