package org.kechinvv.repository

import java.io.File
import java.io.IOException

class LocalRepository(val file: File) {
    lateinit var tests: String
    lateinit var jars: String
    lateinit var sources: String

    init {
        scanTests()
        scanJars()
        scanSources()
    }

    fun scanTests() {}

    fun scanJars() {}

    fun scanSources() {}

    @Throws(IOException::class)
    fun delete(): Boolean {
        return file.deleteRecursively()
    }

}