package org.kechinvv.repository

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kechinvv.entities.InvokeData
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.name
import kotlin.io.path.readLines

abstract class LocalRepository(val file: File) {
    lateinit var tests: String
    lateinit var jars: String
    lateinit var sources: String

    abstract fun build()

    abstract fun runTests()

    fun cleanLibMinerLogs() {
        Files.walk(file.toPath(), 1).filter { it.name.endsWith("libminer.log") }.forEach { Files.delete(it) }
    }

    fun extractTracesFromLogs(): Map<String, List<InvokeData>> {
        val separatedTraces = HashMap<String, MutableList<InvokeData>>()
        Files.walk(file.toPath(), 1).filter { it.name.endsWith("libminer.log") }.forEach { logFile ->
            logFile.readLines().forEach { strInvokeData ->
                val invokeData = Json.decodeFromString<InvokeData>(strInvokeData)
                if (!invokeData.methodData.isStatic)
                    separatedTraces.getOrPut(invokeData.iHash) { mutableListOf() }.add(invokeData)
                else separatedTraces.getOrPut(invokeData.methodData.klass) { mutableListOf() }.add(invokeData)
            }
        }
        separatedTraces.forEach{ (_, trace) -> trace.sortBy { invokeData -> invokeData.date.toLong() }}
        return separatedTraces
    }


    @Throws(IOException::class)
    fun delete(): Boolean {
        return file.deleteRecursively()
    }

}