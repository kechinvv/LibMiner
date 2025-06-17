package org.kechinvv.workflow

import okhttp3.OkHttpClient
import org.kechinvv.analysis.JazzerRunner
import org.kechinvv.analysis.SootManager
import org.kechinvv.config.Configuration
import org.kechinvv.inference.FSMInference
import org.kechinvv.repository.*
import org.kechinvv.storage.Storage
import org.kechinvv.utils.getPathForFuzz
import org.kechinvv.utils.logger

class DefaultWorkflow(val configuration: Configuration) {

    companion object {
        val LOG by logger()
    }

    val storage = Storage(configuration.sourceDB, configuration.cacheSize)
    val sequences = mutableListOf<Iterator<RemoteRepository>>()

    fun run() {
        setupSequences()
        getAndAnalyze()
        inference()
    }

    fun setupSequences() {
        if (configuration.projectsConfiguration.useMvn) sequences.add(
            MvnProjectSequence(
                configuration.projectsConfiguration.targetLibGroup,
                configuration.projectsConfiguration.targetLibName,
                configuration.projectsConfiguration.targetLibVersion,
                OkHttpClient()
            ).iterator()
        )
        if (configuration.projectsConfiguration.useGh) sequences.add(
            GhProjectsSequence(
                configuration.projectsConfiguration,
                OkHttpClient()
            ).iterator()
        )
    }

    fun getAndAnalyze() {
        var limit = if (configuration.countOfProjects == 0) -1 else configuration.countOfProjects
        var sequenceSelector = 0
        while (limit != 0 && sequences[sequenceSelector].hasNext()) {
            val remoteRepo = sequences[sequenceSelector].next()
            val localRepo = remoteRepo.cloneTo(
                configuration.workdir
                    .resolve("${remoteRepo.repositoryData.name}-${remoteRepo.repositoryData.version ?: remoteRepo.repositoryData.author}")
            )
            if (localRepo is LocalRepository) localRepo.build()
            this.analyze(localRepo)

            if (limit > 0) limit--
            sequenceSelector = (sequenceSelector + 1) % sequences.size
        }

    }

    fun analyze(localRepo: AbstractLocalRepository) {
        SootManager.staticExtract(localRepo.getPathForClassFiles(), storage, configuration)
        localRepo.getPathForJarFiles().forEach { targetJar ->
            SootManager.instrumentLibCalls(
                targetJar,
                localRepo.getPathForJarFiles().first(),
                configuration.targetLibExtractingUnit
            )
            try {
                if (localRepo is LocalRepository) localRepo.runTests()
            } catch (e: Throwable) {
                LOG.warn("Failed to run tests for {}", localRepo.path)
            }
            val jazzer = JazzerRunner(configuration.fuzzingExecutions, configuration.fuzzingTimeInSeconds)
            val entryPoints = SootManager.getEntryPoints(targetJar)
            entryPoints.forEach { entryPoint ->
                jazzer.run(localRepo.getJars().toList(), entryPoint.getPathForFuzz(), localRepo.path)
                localRepo.extractTracesFromLogs()
                    .forEach { trace -> if (trace.trace.size > 1) storage.saveTrace(trace) }
                localRepo.cleanLibMinerLogs()
            }
        }
    }

    fun inference() {
        val fsm = FSMInference(configuration.fsmConfiguration, storage)
        fsm.inferenceAll()
    }

}