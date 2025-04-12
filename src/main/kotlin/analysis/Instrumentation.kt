package org.kechinvv.analysis

import soot.G
import soot.PackManager
import soot.Scene
import soot.SootClass.SIGNATURES
import soot.Transform
import soot.options.Options
import java.io.File

class Instrumentation {
    fun runAnalyze(classpath: String): Boolean {
        try {
            init()
            Options.v().set_prepend_classpath(true)
            Options.v().set_allow_phantom_refs(true)
            Options.v().set_process_dir(listOf(classpath))

            val javaPaths = File("javapaths.txt").readText().trim()
            var classPaths = javaPaths.replace(Regex("(\n|\r|\r\n)"), File.pathSeparator)
            classPaths += File.pathSeparator + classpath

            Options.v().set_soot_classpath(classPaths)
            Scene.v().loadNecessaryClasses()
            Scene.v().addBasicClass("java.lang.System", SIGNATURES);
            PackManager.v().runPacks()
            PackManager.v().writeOutput();
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
            return false
        }
    }

    fun init() {
        G.reset()
        if (!PackManager.v().hasPack("jtp.ihash")) PackManager.v().getPack("jtp")
            .add(Transform("jtp.ihash", InstrumentationTransformer("")))
    }
}