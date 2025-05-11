package org.kechinvv.inference

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import guru.nidi.graphviz.parse.Parser
import mint.app.Mint
import org.kechinvv.config.Configuration
import org.kechinvv.storage.Storage
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path

class FSMInference(val mintFilesPath: String, val jsonAndDotFilesPath: String = mintFilesPath, val configuration: Configuration, val storage: Storage) {


    fun inferenceAll(toJson: Boolean = configuration.toJson, unionEnd: Boolean = configuration.unionEnd) {
        val klasses = storage.getClasses()
        klasses.forEach {
            inferenceByClass(it, toJson, unionEnd)
        }
    }

    fun inferenceByClass(klass: String, toJson: Boolean = true, unionEnd: Boolean = configuration.unionEnd) {
        val ids = storage.getTracesIdForClass(klass)
        val methods = storage.getMethodsForClass(klass)
        val klassStr = klass.replace(".", "+")
        val filePathIn = createInputFile(methods, klassStr)
        ids.forEach {
            val trace = storage.getTraceById(it)
            updateFileTrace(trace!!, filePathIn)
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

    fun inferenceFSM(pathIn: String, pathOut: String, k: Int = configuration.kAlg) {
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

    fun createInputFile(methods: HashSet<String>, klass: String): Path {
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
            methods.forEach {
                Files.write(path, listOf(it), StandardCharsets.UTF_8, StandardOpenOption.APPEND)
            }
            // Files.write(Paths.get(path), listOf("end"), StandardCharsets.UTF_8, StandardOpenOption.APPEND)
        } catch (e: IOException) {
            println(e)
        }
        return path
    }

    fun updateFileTrace(jsonTrace: String, filePath: Path) {
        val realTrace: List<String> = Gson().fromJson(jsonTrace, object : TypeToken<List<String>>() {}.type)
        try {
            Files.write(filePath, listOf("trace"), StandardCharsets.UTF_8, StandardOpenOption.APPEND)
            realTrace.forEach {
                Files.write(
                    filePath,
                    listOf(it),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND
                )
            }
            // Files.write(Paths.get(filePath), listOf("end"), StandardCharsets.UTF_8, StandardOpenOption.APPEND)
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