package org.kechinvv.analysis

import org.apache.commons.lang.SystemUtils
import org.kechinvv.utils.JazzerDownloader
import org.kechinvv.utils.logger
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories

class JazzerRunner(val fuzzingExecutions: Int, val fuzzingTimeInSeconds: Int) {
    private val jazzerExecutor: Path = JazzerDownloader().getOrDownload().absolute()

    companion object {
        val LOG by logger()
    }

    fun run(classpathDirs: List<Path>, targetMethod: String, workingDir: Path) {
        val jazzerCpArg = classpathDirs.map { it.toAbsolutePath() }.toList()

            .joinToString(File.pathSeparator)
        val pathNul =  Paths.get(if (SystemUtils.IS_OS_WINDOWS) "trash" else "/dev/null").toAbsolutePath()
        pathNul.createDirectories()
        val jazzerProcess = ProcessBuilder(
            jazzerExecutor.toString(),
            "--cp=\"$jazzerCpArg\"",
            "--keep_going=0",
            "-runs=$fuzzingExecutions",
            "-max_total_time=$fuzzingTimeInSeconds",
            "--autofuzz=$targetMethod",
            "--reproducer_path=\"$pathNul\"",
            "--jvm_args=\"-Duser.dir=${workingDir.toAbsolutePath()}\""
        )

        jazzerProcess.redirectOutput(ProcessBuilder.Redirect.appendTo(File("jazzer.log")))
        jazzerProcess.redirectError(ProcessBuilder.Redirect.appendTo(File("jazzer.log")))
        LOG.info(jazzerProcess.command().joinToString(" "))
        val ps = jazzerProcess.start()
        LOG.debug(ps.pid().toString())
        val res = ps.waitFor((fuzzingTimeInSeconds * 2).toLong(), TimeUnit.SECONDS)
        LOG.debug("fuzzing end = $res")
        ps.destroyForcibly()
    }
}

