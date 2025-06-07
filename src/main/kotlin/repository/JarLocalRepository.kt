package org.kechinvv.repository

import java.nio.file.Path

class JarLocalRepository(val targetJar: Path, path: Path) : AbstractLocalRepository(path)