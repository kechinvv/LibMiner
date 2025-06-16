package org.kechinvv.analysis

import com.charleskorn.kaml.Yaml
import net.lingala.zip4j.ZipFile
import org.kechinvv.config.Configuration
import org.kechinvv.entities.EntryFilter
import org.kechinvv.storage.Storage
import org.kechinvv.utils.isEntryPoint
import soot.*
import soot.options.Options
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors
import kotlin.io.path.*

object SootManager {

    private var filters = Files.walk(Paths.get("entry_points_filters")).filter { it.extension == "yaml" }
        .map { Yaml.default.decodeFromString(EntryFilter.serializer(), it.readText()) }.collect(Collectors.toSet())

    private var javaPaths = File("javapaths.txt").readText().trim().replace(Regex("(\n|\r|\r\n)"), File.pathSeparator)

    private val helperClass = Paths.get(
        this::class::class.java.getResource(
            "/LibMinerInstrumentationHelper.class"
        )!!.toURI()
    )

    private fun defaultSetup(pathsToTarget: List<Path>) {
        G.reset()
        Options.v().set_prepend_classpath(true)
        Options.v().set_allow_phantom_refs(true)
        Options.v().set_src_prec(Options.src_prec_class)
        Options.v().set_process_dir(pathsToTarget.flatMap { pathToTarget ->
            Files.walk(pathToTarget).filter { it.isDirectory() || it.extension == "jar" }.map { it.toString() }.toList()
        })
        val jarPaths = mutableSetOf<String>()
        pathsToTarget.forEach { path ->
            path.walk().filter { it.extension == "jar" }.forEach { jarPaths.add(it.parent.toString()) }
        }
        if (jarPaths.isNotEmpty()) Options.v().set_process_jar_dir(jarPaths.toList())
        Options.v().set_java_version(21)


        var classPaths = javaPaths
        if (classPaths == "") classPaths = pathsToTarget.joinToString(File.pathSeparator)
        else classPaths += File.pathSeparator + pathsToTarget.joinToString(File.pathSeparator)

        Options.v().set_soot_classpath(classPaths)
    }


    fun staticExtract(pathsToTarget: List<Path>, storage: Storage, configuration: Configuration) {
        defaultSetup(pathsToTarget)

        Options.v().set_whole_program(true)
        Options.v().setPhaseOption("cg.spark", "enabled:true")
        Options.v().setPhaseOption("cg", "verbose:true")

        if (!PackManager.v().hasPack("wjtp.ifds.SequenceCollector")) PackManager.v().getPack("wjtp")
            .add(Transform("wjtp.ifds.SequenceCollector", SequenceCollectorTransformer(storage, configuration)))

        Scene.v().loadNecessaryClasses()
        Scene.v().entryPoints = getEntryPointsInProcess().toList()
        SequenceCollectorTransformer.LOG.info("Entry points size: {}", Scene.v().entryPoints.size)
        PackManager.v().runPacks()
    }


    fun instrumentLibCalls(pathToTarget: Path, outputDir: Path, libPatterns: Set<String>) {
        defaultSetup(listOf(pathToTarget))
        val jar = pathToTarget.extension == "jar"

        Options.v().set_output_jar(jar)
        Options.v().set_output_dir(outputDir.toString())

        val notInstrumentedYet =
            if (jar) saveMetaInfAndAddHelperToJar(pathToTarget) else addHelperToClasses(pathToTarget)
        if (!notInstrumentedYet) return
        Scene.v().loadNecessaryClasses()

        if (!PackManager.v().hasPack("jtp.ihash")) PackManager.v().getPack("jtp")
            .add(Transform("jtp.ihash", InstrumentationTransformer(libPatterns)))

        PackManager.v().runPacks()
        PackManager.v().writeOutput()
        G.reset()
        System.gc()
        Thread.sleep(1000)
        if (jar) restoreMetaInf(pathToTarget)
    }

    @OptIn(ExperimentalPathApi::class)
    private fun restoreMetaInf(pathToTarget: Path) {
        val metainfSaved = pathToTarget.parent.resolve("META-INF")
        if (metainfSaved.exists())
            ZipFile(pathToTarget.toFile()).addFolder(metainfSaved.toFile())
        metainfSaved.deleteRecursively()
    }

    //return false if already instrumented
    private fun saveMetaInfAndAddHelperToJar(pathToTarget: Path): Boolean {
        val jarFile = ZipFile(pathToTarget.toFile())
        val helperInZip = jarFile.getFileHeader(helperClass.fileName.toString())
        if (helperInZip != null) {
            return false
        }
        jarFile.addFile(helperClass.toFile())
        jarFile.extractFile("META-INF/", pathToTarget.parent.toString())
        return true
    }

    //return false if already instrumented
    private fun addHelperToClasses(pathToTarget: Path): Boolean {
        val targetHelper = pathToTarget.resolve("LibMinerInstrumentationHelper.class")
        if (Files.exists(targetHelper)) return false
        Files.copy(
            helperClass,
            targetHelper,
            StandardCopyOption.REPLACE_EXISTING
        )
        return true
    }


    fun getEntryPoints(classpath: Path): Set<SootMethod> {
        defaultSetup(listOf(classpath))
        Scene.v().loadNecessaryClasses()
        return getEntryPointsInProcess()
    }

    private fun getEntryPointsInProcess(): Set<SootMethod> {
        val entryPoints = HashSet<SootMethod>()
        Scene.v().applicationClasses.forEach { klass ->
            klass.methods.forEach {
                if (it.isEntryPoint(filters)) entryPoints.add(it)
            }
        }
        return entryPoints
    }

}