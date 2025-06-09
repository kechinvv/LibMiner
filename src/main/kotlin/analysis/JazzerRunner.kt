package org.kechinvv.analysis

import org.apache.commons.lang.SystemUtils
import org.kechinvv.utils.JazzerDownloader
import org.kechinvv.utils.logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolute
import kotlin.io.path.extension

class JazzerRunner(val fuzzingExecutions: Int, val fuzzingTimeInSeconds: Int) {
    private val jazzerExecutor: Path = JazzerDownloader().getOrDownload().absolute()

    companion object {
        val LOG by logger()
    }

    fun run(classpathDirs: List<Path>, targetMethod: String) {
        val jazzerCpArg = classpathDirs.flatMap { cp -> Files.walk(cp).filter { it.extension == "jar" }.toList() }
            .joinToString(File.pathSeparator)
        val devNull = if (SystemUtils.IS_OS_WINDOWS) "nul" else "/dev/null"

        val jazzerProcess = ProcessBuilder(
            jazzerExecutor.toString(),
            "--cp=\"$jazzerCpArg\"",
            "--keep_going=0",
            "-runs=$fuzzingExecutions",
            "-max_total_time=$fuzzingTimeInSeconds",
            "--autofuzz=$targetMethod",
            "--reproducer_path=$devNull",
        )

        jazzerProcess.redirectOutput(ProcessBuilder.Redirect.appendTo(File("jazzer.log")))
        jazzerProcess.redirectError(ProcessBuilder.Redirect.appendTo(File("jazzer.log")))
        LOG.info(jazzerProcess.command().joinToString(" "))
        val ps = jazzerProcess.start()
        LOG.debug(ps.pid().toString())
        val res = ps.waitFor(60 * 2, TimeUnit.SECONDS)
        LOG.debug("fuzzing end = $res")
        ps.destroyForcibly()
    }
}

