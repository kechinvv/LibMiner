package org.kechinvv.workflow

import okhttp3.OkHttpClient
import org.kechinvv.repository.JarLocalRepository
import org.kechinvv.repository.MvnProjectSequence
import org.kechinvv.storage.Storage
import java.nio.file.Paths

class TestOkhttpWorkflow {

    fun getPrjs() {
        val workdir = Paths.get("workdir/okhttpmvn/")
        val storage = Storage(workdir.resolve("db.db"))

        MvnProjectSequence("com.squareup.okhttp", "okhttp", "2.7.5", OkHttpClient()).takeWhile { workdir.toFile().listFiles().size != 101 }
            .filter { !storage.repoWasFoundIgnoreVersion(it) }.forEach { repo ->
                storage.saveRepo(repo)
                try {
                        repo.cloneTo(workdir.resolve("${repo.repositoryData.name}-${repo.repositoryData.version}")) as JarLocalRepository
                }catch (e: Exception) {
                    println(e.message)
                    e.printStackTrace()
                }
            }

    }
}