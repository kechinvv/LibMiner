package org.kechinvv.analysis

import org.kechinvv.utils.JazzerDownloader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolute
import kotlin.io.path.extension

class JazzerRunner {
    private val jazzerExecutor: Path = JazzerDownloader().getOrDownload().absolute()

    fun run(classpathDirs: List<Path>, targetMethod: String) {
        val jazzerCpArg = classpathDirs.flatMap { cp -> Files.walk(cp).filter { it.extension == "jar" }.toList() }
            .joinToString(File.pathSeparator)

        val jazzerProcess = ProcessBuilder(
            jazzerExecutor.toString(),
            "--cp=\"$jazzerCpArg\"",
            "--keep_going=0",
            "-runs=100",
            "-max_total_time=60",
            "--autofuzz=$targetMethod"
        )

        jazzerProcess.redirectOutput(ProcessBuilder.Redirect.appendTo(File("jazzer.log")))
        jazzerProcess.redirectError(ProcessBuilder.Redirect.appendTo(File("jazzer.log")))
        println(jazzerProcess.command().joinToString(" "))
        val ps = jazzerProcess.start()
        println(ps.pid())
        val res = ps.waitFor(60 * 2, TimeUnit.SECONDS)
        println(res)
        ps.destroyForcibly()
    }
}

