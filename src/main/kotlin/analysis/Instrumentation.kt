package org.kechinvv.analysis

import net.lingala.zip4j.ZipFile
import soot.G
import soot.PackManager
import soot.Scene
import soot.Transform
import soot.options.Options
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively


class Instrumentation {
    @OptIn(ExperimentalPathApi::class)
    fun runAnalyze(programPath: Path, libPatterns: Set<String>, outputDir: Path, jar: Boolean): Boolean {
        try {
            G.reset()
            Options.v().set_prepend_classpath(true)
            Options.v().set_allow_phantom_refs(true)
            Options.v().set_process_dir(listOf(programPath.toString()))
            Options.v().set_output_jar(jar)
            Options.v().set_output_dir(outputDir.toString())
            Options.v().set_java_version(21)

            val javaPaths = File("javapaths.txt").readText().trim()
            val helperClass = Paths.get(
                this::class::class.java.getResource(
                    "/LibMinerInstrumentationHelper.class"
                )!!.toURI()
            )

            val sootClassPath = programPath.toString() + File.pathSeparator + javaPaths.replace(
                Regex("(\n|\r|\r\n)"),
                File.pathSeparator
            )

            if (jar) {
                val jarFile = ZipFile(programPath.toFile())
                val helperInZip = jarFile.getFileHeader(helperClass.fileName.toString())
                if (helperInZip != null) {
                    return true
                }
                jarFile.addFile(helperClass.toFile())
                jarFile.extractFile("META-INF/", programPath.parent.toString())


            } else {
                val targetHelper = programPath.resolve("LibMinerInstrumentationHelper.class")
                if (Files.exists(targetHelper)) return true
                Files.copy(
                    helperClass,
                    targetHelper,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }


            Options.v().set_soot_classpath(sootClassPath)

            Scene.v().loadNecessaryClasses()

            loadTransform(libPatterns)

            PackManager.v().runPacks()
            PackManager.v().writeOutput()
            G.reset()
            System.gc()
            Thread.sleep(1000)

            if (jar) {
                val metainfSaved = programPath.parent.resolve("META-INF")
                ZipFile(programPath.toFile()).addFolder(metainfSaved.toFile())
                metainfSaved.deleteRecursively()
            }

            return true
        } catch (e: Throwable) {
            e.printStackTrace()
            return false
        }
    }

    fun loadTransform(libPatterns: Set<String>) {
        if (!PackManager.v().hasPack("jtp.ihash")) PackManager.v().getPack("jtp")
            .add(
                Transform(
                    "jtp.ihash",
                    InstrumentationTransformer(
                        libPatterns
                    )
                )
            )
    }
}