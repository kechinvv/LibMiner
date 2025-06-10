package org.kechinvv.workflow

import okhttp3.OkHttpClient
import org.kechinvv.config.Configuration
import org.kechinvv.repository.GhProjectsSequence
import java.nio.file.Paths

class StandardWorkflow {

    fun run() {
        val configuration = Configuration()
        val sequence = GhProjectsSequence(OkHttpClient(), configuration)
        sequence.take(configuration.countOfProjects).forEach { prj ->
            val local = prj.cloneTo(Paths.get(configuration.workdir).resolve(prj.name))
        }
    }
}