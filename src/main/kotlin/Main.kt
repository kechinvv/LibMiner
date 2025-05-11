package org.kechinvv

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import org.kechinvv.analysis.Instrumentation
import org.kechinvv.config.Configuration
import org.kechinvv.repository.GradleLocalRepository
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths

fun main() {
    val configuration =
        Yaml.default.decodeFromStream(Configuration.serializer(), FileInputStream("config.yaml"))
    val classpath_jar =
        Paths.get("C:\\Users\\valer\\IdeaProjects\\libminer_test\\build\\libs\\libminer_test-1.0-SNAPSHOT.jar")



    val classpath = "C:\\Users\\valer\\IdeaProjects\\libminer_test\\build\\classes\\java\\main"
    val mvn_classpath = "C:\\Users\\valer\\IdeaProjects\\libminer_mvtest\\target\\classes"
    val local = GradleLocalRepository(File("C:\\Users\\valer\\IdeaProjects\\libminer_test"), configuration)
    local.build()
    val i = Instrumentation().runAnalyze(classpath_jar, "java", classpath_jar, true)





//    local.cleanLibMinerLogs()
//    Instrumentation().runAnalyze(classpath, "java", classpath, false)
//    local.runTests()
//    println(local.extractTracesFromLogs())
//    val target = Paths.get("./downloaded_reps")
//    target.createDirectory()
//    RemoteLib("com.atlassian.bamboo", "bamboo-specs-runner", "10.2.1").cloneTo(target)
}