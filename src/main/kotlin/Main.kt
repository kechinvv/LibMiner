package org.kechinvv

import org.kechinvv.analysis.Instrumentation
import org.kechinvv.config.Configuration
import org.kechinvv.repository.LocalRepository
import org.kechinvv.workflow.TestOkhttpWorkflow
import java.nio.file.Paths

fun main() {
//    val configuration = Configuration()
//    val classpath_jar =
//        Paths.get("C:\\Users\\valer\\IdeaProjects\\libminer_test\\build\\libs\\libminer_test-1.0-SNAPSHOT.jar")
//
//
//    val classpath_jar2 =
//        Paths.get("C:\\Users\\valer\\IdeaProjects\\libminer_test\\build\\libs\\all-in-one-jar-1.0-SNAPSHOT.jar")
//
//    val classpath = Paths.get("C:\\Users\\valer\\IdeaProjects\\libminer_test\\build\\classes\\java\\main")
//    val mvn_classpath = "C:\\Users\\valer\\IdeaProjects\\libminer_mvtest\\target\\classes"
//    val local = LocalRepository(Paths.get("C:\\Users\\valer\\IdeaProjects\\libminer_test"), configuration)
//    local.build()
//    val i = Instrumentation().runAnalyze(classpath, "secret", classpath, false)
//    local.runTests()





//    local.cleanLibMinerLogs()
//    Instrumentation().runAnalyze(classpath, "java", classpath, false)
//    local.runTests()
//    println(local.extractTracesFromLogs())
//    val target = Paths.get("./downloaded_reps")
//    target.createDirectory()
//    RemoteLib("com.atlassian.bamboo", "bamboo-specs-runner", "10.2.1").cloneTo(target)
    TestOkhttpWorkflow().collectStaticTraces()
}