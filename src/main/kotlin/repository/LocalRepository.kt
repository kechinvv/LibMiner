package org.kechinvv.repository

import java.io.File
import java.io.IOException

abstract class LocalRepository(val file: File) {
    lateinit var tests: String
    lateinit var jars: String
    lateinit var sources: String

    abstract fun build()

    abstract fun runTests()


    @Throws(IOException::class)
    fun delete(): Boolean {
        return file.deleteRecursively()
    }

}