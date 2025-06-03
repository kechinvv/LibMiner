package org.kechinvv.inference

import guru.nidi.graphviz.parse.Parser
import mint.app.Mint
import org.kechinvv.config.Configuration
import org.kechinvv.entities.MethodData
import org.kechinvv.holders.TraceHolder
import org.kechinvv.storage.Storage
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path

class FSMInference(
    val mintFilesPath: String,
    val jsonAndDotFilesPath: String = mintFilesPath,
    val configuration: Configuration,
    val storage: Storage
) {


    fun inferenceAll(toJson: Boolean = configuration.toJson, unionEnd: Boolean = configuration.unionEnd) {
        val klasses = storage.getClasses()
        klasses.forEach {
            inferenceByClass(it, false, toJson, unionEnd)
            inferenceByClass(it, true, toJson, unionEnd)
        }
    }

    fun inferenceByClass(
        klass: String,
        staticCalls: Boolean,
        toJson: Boolean = true,
        unionEnd: Boolean = configuration.unionEnd
    ) {
        val traces = storage.getTracesForClass(klass, staticCalls)
        //val methods = storage.getMethodsForClass(klass, staticCalls)
        val methods = traces.flatMap { traceHolder -> traceHolder.trace }.toHashSet()
        val klassStr = klass.replace(".", "+")
        val filePathIn = createInputFile(methods, klassStr)
        traces.forEach { trace ->
            updateFileTrace(trace, filePathIn)
        }

        val filePathOutDot = Path(jsonAndDotFilesPath, klassStr + "Out.dot")
        Files.deleteIfExists(filePathOutDot)
        inferenceFSM(filePathIn.toString(), filePathOutDot.toString())

        val filePathOut = Path(jsonAndDotFilesPath, "$klassStr.json")
        val fsm = dotToFSM(filePathOutDot, klass)
        if (unionEnd) {
            val filePathOutUnionDot = Path(jsonAndDotFilesPath, klassStr + "OutUnion.dot")
            fsm.unionEnd()
            fsm.toDot(filePathOutUnionDot)
        }
        if (toJson) fsm.toJson(filePathOut)
    }

    fun inferenceFSM(pathIn: String, pathOut: String, k: Int = configuration.kTail) {
        Files.createDirectories(Paths.get(jsonAndDotFilesPath))
        Mint.main(
            arrayOf(
                "-input",
                pathIn,
                "-k",
                k.toString(),
                "-strategy",
                "ktails",
                "-visout",
                pathOut
            )
        )
    }

    fun createInputFile(methods: HashSet<MethodData>, klass: String): Path {
        val path = Path(mintFilesPath, klass + "In.txt")
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
                    listOf(methodData.name),
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
        return FSM(klass, g.edges(), g.nodes(), configuration)
    }

}