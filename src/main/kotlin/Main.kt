package org.kechinvv

import org.kechinvv.config.Configuration
import org.kechinvv.repository.JarLocalRepository
import org.kechinvv.workflow.DefaultWorkflow
import java.nio.file.Paths

fun main() {
    val config = Configuration()
    config.targetLibExtractingUnit = setOf("okhttp", "okhttp3", "com.squareup.okhttp")
    val repo = JarLocalRepository(
        Paths.get("C:\\Users\\valer\\IdeaProjects\\LibMiner\\workdir_instr\\okhttpmvn\\aca-java-1.0.0\\aca-java-1.0.0.jar"),
        Paths.get("C:\\Users\\valer\\IdeaProjects\\LibMiner\\workdir_instr\\okhttpmvn\\aca-java-1.0.0")
    )

    val defaultWorkflow = DefaultWorkflow(config)
    defaultWorkflow.analyze(repo)
}