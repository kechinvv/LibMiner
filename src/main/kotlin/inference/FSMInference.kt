package org.kechinvv.inference

import guru.nidi.graphviz.parse.Parser
import mint.app.Mint
import org.kechinvv.config.FsmConfiguration
import org.kechinvv.entities.MethodData
import org.kechinvv.holders.TraceHolder
import org.kechinvv.storage.Storage
import org.kechinvv.utils.logger
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.function.Predicate
import kotlin.io.path.createDirectories

class FSMInference(
    val configuration: FsmConfiguration,
    val storage: Storage
) {
    init {
        configuration.mintFilesPath.createDirectories()
        configuration.jsonAndDotFilesPath.createDirectories()
    }

    companion object {
        val LOG by logger()
    }


    fun inferenceAll(
        filter: Predicate<TraceHolder>? = null,
        toJson: Boolean = configuration.toJson,
        unionEnd: Boolean = configuration.unionEnd,
    ) {
        val klasses = storage.getClasses()
        LOG.info("classes = {}", klasses)

        klasses.forEach {
            try {
                inferenceByClass(it, filter, toJson, unionEnd)
            } catch (e: Throwable) {
                e.printStackTrace()
                LOG.warn("Failed to inference {} model", it)
            }
        }
    }

    fun inferenceByClass(
        klass: String,
        filter: Predicate<TraceHolder>? = null,
        toJson: Boolean = configuration.toJson,
        unionEnd: Boolean = configuration.unionEnd
    ): InferenceResult {
        val traces = storage.getTracesForClass(klass, filter = filter)
        val methods = traces.flatMap { traceHolder -> traceHolder.trace }.toHashSet()
        val klassStr = klass.replace(".", "+")
        val filePathIn = createInputFile(methods, klassStr)
        traces.forEach { trace ->
            updateFileTrace(trace, filePathIn)
        }

        val filePathOutDot = configuration.jsonAndDotFilesPath.resolve("${klassStr}Out.dot")
        Files.deleteIfExists(filePathOutDot)
        inferenceFSM(filePathIn.toString(), filePathOutDot.toString())

        val filePathOut = configuration.jsonAndDotFilesPath.resolve("${klassStr}.json")
        val fsm = dotToFSM(filePathOutDot, klass)
        var filePathOutUnionDot: Path? = null
        if (unionEnd) {
            filePathOutUnionDot = configuration.jsonAndDotFilesPath.resolve("${klassStr}OutHandled.dot")
            fsm.unionEnd()
            fsm.toDot(filePathOutUnionDot)
        }
        if (toJson) fsm.toJson(filePathOut)
        return InferenceResult(filePathOutDot, filePathOutUnionDot, filePathOut)
    }

    fun inferenceFSM(
        pathIn: String,
        pathOut: String,
        k: Int = configuration.kTail,
        strategy: String = configuration.strategy
    ) {
        configuration.jsonAndDotFilesPath.createDirectories()
        Mint.main(
            arrayOf(
                "-input",
                pathIn,
                "-k",
                k.toString(),
                "-strategy",
                strategy,
                "-visout",
                pathOut
            )
        )
    }

    fun createInputFile(methods: HashSet<MethodData>, klass: String): Path {
        val path = configuration.mintFilesPath.resolve("${klass}In.txt")
        try {
            Files.deleteIfExists(path)
            Files.write(
                path,
                listOf("types"),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            )
            methods.forEach { methodData ->
                val methodText = if (configuration.useSignature) methodData.getSignature() else methodData.name
                Files.write(path, listOf(methodText), StandardCharsets.UTF_8, StandardOpenOption.APPEND)
            }
        } catch (e: IOException) {
            println(e)
        }
        return path
    }

    fun updateFileTrace(traceHolder: TraceHolder, filePath: Path) {
        try {
            Files.write(filePath, listOf("trace"), StandardCharsets.UTF_8, StandardOpenOption.APPEND)
            traceHolder.trace.forEach { methodData ->
                val methodText = if (configuration.useSignature) methodData.getSignature() else methodData.name
                Files.write(
                    filePath,
                    listOf(methodText),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND
                )
            }
        } catch (e: IOException) {
            println(e)
        }
    }

    fun dotToFSM(pathDot: Path, klass: String): FSM {
        val dot = pathDot.toFile().inputStream()
        val g = Parser().read(dot)
        return FSM(klass, g.edges(), g.nodes())
    }

}