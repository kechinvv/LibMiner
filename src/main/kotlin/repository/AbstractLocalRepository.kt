package org.kechinvv.repository

import kotlinx.serialization.json.Json
import org.kechinvv.config.Configuration
import org.kechinvv.entities.InvokeData
import org.kechinvv.entities.MethodData
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.name
import kotlin.io.path.readLines

abstract class AbstractLocalRepository(val file: File) {
    lateinit var tests: String
    lateinit var jars: String
    lateinit var sources: String


    fun cleanLibMinerLogs() {
        Files.walk(file.toPath(), 1).filter { it.name.endsWith("libminer.log") }.forEach { Files.delete(it) }
    }

    fun extractTracesFromLogs(): Map<String, List<MethodData>> {
        val separatedTraces = HashMap<String, MutableList<InvokeData>>()
        Files.walk(file.toPath(), 1).filter { it.name.endsWith("libminer.log") }.forEach { logFile ->
            logFile.readLines().forEach { strInvokeData ->
                val invokeData = Json.decodeFromString<InvokeData>(strInvokeData)
                if (!invokeData.methodData.isStatic)
                    separatedTraces.getOrPut(invokeData.iHash) { mutableListOf() }.add(invokeData)
                else separatedTraces.getOrPut(invokeData.methodData.klass) { mutableListOf() }.add(invokeData)
            }
        }
        separatedTraces.forEach { (_, trace) -> trace.sortBy { invokeData -> invokeData.date.toLong() } }
        return separatedTraces.mapValues { pair -> pair.value.map { invokeData -> invokeData.methodData } }
    }


    @Throws(IOException::class)
    fun delete(): Boolean {
        return file.deleteRecursively()
    }

    companion object {
        fun getLocalRepository(file: File, configuration: Configuration): AbstractLocalRepository {
            return GradleLocalRepository(file, configuration)
        }
    }

}