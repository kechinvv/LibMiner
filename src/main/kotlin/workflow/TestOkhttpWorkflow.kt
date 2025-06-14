package org.kechinvv.workflow

import okhttp3.OkHttpClient
import org.kechinvv.analysis.JazzerRunner
import org.kechinvv.analysis.SceneExtractor
import org.kechinvv.analysis.SootManager
import org.kechinvv.config.Configuration
import org.kechinvv.inference.FSMInference
import org.kechinvv.repository.JarLocalRepository
import org.kechinvv.repository.MvnProjectSequence
import org.kechinvv.storage.Storage
import org.kechinvv.utils.ExtractMethod
import org.kechinvv.utils.getPathForFuzz
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.*

class TestOkhttpWorkflow {
    val config = Configuration()
    val workdir = Paths.get("workdir/okhttpmvn/")
    val workdir_instr = Paths.get("workdir_instr/okhttpmvn/")

    val storage = Storage(workdir.resolve("db.db"))

    fun getPrjs() {
        MvnProjectSequence("com.squareup.okhttp", "okhttp", "2.7.5", OkHttpClient()).takeWhile {
            workdir.toFile().listFiles().size != 101
        }
            .filter { !storage.repoWasFoundIgnoreVersion(it) }.forEach { repo ->
                storage.saveRepo(repo)
                try {
                    repo.cloneTo(workdir.resolve("${repo.repositoryData.name}-${repo.repositoryData.version}")) as JarLocalRepository
                } catch (e: Exception) {
                    println(e.message)
                    e.printStackTrace()
                }
            }

    }

    fun collectStaticTraces() {
        val extractor = SceneExtractor(config, storage)
        config.targetLibExtractingUnit = setOf("okhttp", "okhttp3", "com.squareup.okhttp")

        var total = 0
        Files.walk(workdir, 2).filter { it != workdir && it.isDirectory() }.forEach { dir ->

            try {
                val targetJar = dir.walk().first { it.nameWithoutExtension == dir.name && it.extension == "jar" }
                val jarRepo = JarLocalRepository(targetJar, dir)
                println(jarRepo.targetJar)
                println(total)
                extractor.runAnalyze(jarRepo.targetJar)
                total++
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        println(total)
    }

    fun traceCollectSimple() {
        val extractor = SceneExtractor(config, storage)

        extractor.runAnalyze(Paths.get("C:\\Users\\valer\\IdeaProjects\\libminer_test\\build\\libs\\libminer_test-1.0-SNAPSHOT.jar"))
    }

    fun inference() {
        config.fsmConfiguration.jsonAndDotFilesPath = workdir
        config.fsmConfiguration.mintFilesPath = workdir
        config.fsmConfiguration.kTail = 1
        val fsmInference = FSMInference(config.fsmConfiguration, storage)
        fsmInference.inferenceByClass("java.io.File")
    }


    fun collectStaticTracesNew() {
        config.targetLibExtractingUnit = setOf("okhttp", "okhttp3", "com.squareup.okhttp")

        var total = 0
        Files.walk(workdir, 2).filter { it != workdir && it.isDirectory() }.forEach { dir ->

            try {
                val targetJar = dir.walk().first { it.nameWithoutExtension == dir.name && it.extension == "jar" }
                val jarRepo = JarLocalRepository(targetJar, dir)
                println(jarRepo.targetJar)
                println(total)
                SootManager.staticExtract(jarRepo.targetJar, storage, config)
                total++
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        println(total)
    }

    fun collectDynamicTraces() {
        config.targetLibExtractingUnit = setOf("okhttp", "okhttp3", "com.squareup.okhttp")
        val jazzerRunner = JazzerRunner(100000, 300)
        var total = 0
        Files.walk(workdir_instr, 2).filter { it != workdir_instr && it.isDirectory() }.forEach { dir ->

            try {
                val targetJar = dir.walk().first { it.nameWithoutExtension == dir.name && it.extension == "jar" }
                val jarRepo = JarLocalRepository(targetJar, dir)
                println(jarRepo.targetJar)
                println(total)
                val entryPoints = SootManager.getEntryPoints(jarRepo.targetJar)
                println("Size: ${entryPoints.size}")
                SootManager.instrumentLibCalls(targetJar, targetJar, true, config.targetLibExtractingUnit)
                entryPoints.forEach {
                    jazzerRunner.run(
                        listOf(jarRepo.path),
                        it.getPathForFuzz(),
                        jarRepo.path
                    )
                    val traces = jarRepo.extractTracesFromLogs()
                    println(traces)
                    traces.forEach { trace -> storage.saveTrace(trace.value, trace.value.first().klass, ExtractMethod.DYNAMIC) }
                    jarRepo.cleanLibMinerLogs()
                }
                total++
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        println(total)
    }
}