package org.kechinvv.analysis

import com.charleskorn.kaml.Yaml
import org.kechinvv.config.Configuration
import org.kechinvv.entities.EntryFilter
import org.kechinvv.storage.Storage
import org.kechinvv.utils.isEntryPoint
import org.kechinvv.utils.logger
import soot.*
import soot.options.Options
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.extension
import kotlin.io.path.readText


class SceneExtractor(val configuration: Configuration, val storage: Storage) {

    companion object {
        val LOG by logger()
    }
    private val filters = Files.walk(Paths.get("entry_points_filters")).filter { it.extension == "yaml" }
        .map { Yaml.default.decodeFromString(EntryFilter.serializer(), it.readText()) }.collect(Collectors.toSet())


    fun runAnalyze(classpath: Path): Boolean {
        try {
            init()
            Options.v().set_prepend_classpath(true)
            Options.v().set_whole_program(true)
            Options.v().set_allow_phantom_refs(true)
            Options.v().set_src_prec(Options.src_prec_class)
            Options.v().set_process_dir(listOf(classpath.toString()))
            Options.v().set_process_jar_dir(listOf(classpath.parent.toString()))
            Options.v().set_output_format(Options.output_format_jimple)
            Options.v().setPhaseOption("cg.spark", "enabled:true")
            Options.v().setPhaseOption("cg", "verbose:true")

            val javaPaths = File("javapaths.txt").readText().trim()
            var classPaths = javaPaths.replace(Regex("(\n|\r|\r\n)"), File.pathSeparator)
            if (classPaths == "") classPaths = classpath.toString()
            else classPaths += File.pathSeparator + classpath.toString()

            Options.v().set_soot_classpath(classPaths)
            Scene.v().loadNecessaryClasses()
            Scene.v().entryPoints = mutableListOf<SootMethod>()
            collectEntryPointsTo(Scene.v().entryPoints)
            SequenceCollectorTransformer.LOG.info("Entry points size: {}, entrypoints: {}", Scene.v().entryPoints.size, Scene.v().entryPoints)
            SequenceCollectorTransformer.LOG.info("Application classes: {}", Scene.v().entryPoints.filter { it.declaringClass.isApplicationClass }.toList().size)
            PackManager.v().runPacks()
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
            return false
        }
    }

    fun init() {
        G.reset()
        if (!PackManager.v().hasPack("wjtp.ifds.SequenceCollector")) PackManager.v().getPack("wjtp")
            .add(Transform("wjtp.ifds.SequenceCollector", SequenceCollectorTransformer(storage, configuration)))
    }

    private fun collectEntryPointsTo(entryPoints: MutableCollection<SootMethod>) {
        Scene.v().applicationClasses.forEach { klass ->
            klass.methods.forEach {
                if (it.isEntryPoint(filters)) entryPoints.add(it)
            }
        }
    }

}