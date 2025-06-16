package org.kechinvv.repository

import kotlinx.serialization.json.Json
import org.kechinvv.entities.InvokeData
import org.kechinvv.holders.TraceHolder
import org.kechinvv.utils.ExtractMethod
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.*

abstract class AbstractLocalRepository(val path: Path) {

    fun cleanLibMinerLogs() {
        Files.walk(path).filter { it.name.endsWith("libminer.log") }.forEach { Files.delete(it) }
    }

    fun extractTracesFromLogs(): Set<TraceHolder> {
        val separatedTraces = HashMap<String, MutableList<InvokeData>>()
        Files.walk(path).filter { it.name.endsWith("libminer.log") }.forEach { logFile ->
            logFile.readLines().forEach { strInvokeData ->
                val invokeData = Json.decodeFromString<InvokeData>(strInvokeData)
                separatedTraces.getOrPut(invokeData.uuid + invokeData.iHash + invokeData.methodData.klass) { mutableListOf() }
                    .add(invokeData)
            }
        }
        separatedTraces.forEach { (_, trace) -> trace.sortBy { invokeData -> invokeData.date.toLong() } }
        return separatedTraces
            .map { rowTrace -> rowTrace.value.sortedBy { it.date }.map { it.methodData } }
            .groupingBy { it }.eachCount()
            .map { TraceHolder(it.key, ExtractMethod.DYNAMIC, it.value) }
            .toSet()
    }

    fun getJars(): Set<Path> {
        return Files.walk(path).filter { it.extension == "jar" }.collect(Collectors.toSet())
    }


    @OptIn(ExperimentalPathApi::class)
    @Throws(IOException::class)
    fun delete(): Boolean {
        path.deleteRecursively()
        return path.exists()
    }

}