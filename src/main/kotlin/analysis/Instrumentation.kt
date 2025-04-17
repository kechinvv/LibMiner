package org.kechinvv.analysis

import soot.G
import soot.PackManager
import soot.Scene
import soot.SootClass.SIGNATURES
import soot.Transform
import soot.options.Options
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


class Instrumentation {
    fun runAnalyze(classpath: String, lib: String, outputDir: String, jar: Boolean): Boolean {
        try {
            init(lib)
            Options.v().set_prepend_classpath(true)
            Options.v().set_allow_phantom_refs(true)
            Options.v().set_process_dir(listOf(classpath))
            Options.v().set_output_jar(jar)
            Options.v().set_output_dir(outputDir)
            Options.v().set_java_version(11)

            val javaPaths = File("javapaths.txt").readText().trim()
            var classPaths = javaPaths.replace(Regex("(\n|\r|\r\n)"), File.pathSeparator)
            classPaths += File.pathSeparator + classpath + File.pathSeparator

            Files.copy(
                Paths.get(this::class::class.java.getResource("/LibMinerInstrumentationHelper.class")!!.toURI()),
                Paths.get(classpath, "LibMinerInstrumentationHelper.class"),
                StandardCopyOption.REPLACE_EXISTING
            )
            Options.v().set_soot_classpath(classPaths)


            Scene.v().addBasicClass("LibMinerInstrumentationHelper", SIGNATURES)
            Scene.v().loadNecessaryClasses()

            PackManager.v().runPacks()
            PackManager.v().writeOutput();
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
            return false
        }
    }

    fun init(lib: String) {
        G.reset()
        if (!PackManager.v().hasPack("jtp.ihash")) PackManager.v().getPack("jtp")
            .add(
                Transform(
                    "jtp.ihash",
                    InstrumentationTransformer(
                        lib,
                        "C:\\Users\\valer\\IdeaProjects\\libminer_test"
                    )
                )
            )
    }
}