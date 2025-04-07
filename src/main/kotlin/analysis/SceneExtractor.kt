package org.kechinvv.analysis

import org.kechinvv.config.Configuration
import org.kechinvv.storage.Storage
import soot.G
import soot.PackManager
import soot.Scene
import soot.Transform
import soot.options.Options
import java.io.File


class SceneExtractor(val lib: String, val configuration: Configuration, val storage: Storage) {


    fun runAnalyze(classpath: String): Boolean {
        try {
            init()
            Options.v().set_prepend_classpath(true)
            Options.v().set_whole_program(true)
            Options.v().set_allow_phantom_refs(true)
            Options.v().set_src_prec(Options.src_prec_only_class)
            Options.v().set_process_dir(listOf(classpath))
            Options.v().set_output_format(Options.output_format_jimple)
            Options.v().setPhaseOption("cg.spark", "enabled:true")

            val javaPaths = File("javapaths.txt").readText().trim()
            var classPaths = javaPaths.replace(Regex("(\n|\r|\r\n)"), File.pathSeparator)
            if (classPaths == "") classPaths = classpath
            else classPaths += File.pathSeparator + classpath

            Options.v().set_soot_classpath(classPaths)
            Scene.v().loadNecessaryClasses()
            PackManager.v().runPacks()
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
            return false
        }
    }

    fun init() {
        G.reset()
        if (!PackManager.v().hasPack("wjtp.ifds")) PackManager.v().getPack("wjtp")
            .add(Transform("wjtp.ifds.SequenceCollector", SequenceCollector(lib, storage, configuration)))
    }


}