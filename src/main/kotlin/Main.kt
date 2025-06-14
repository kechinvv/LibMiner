package org.kechinvv

import org.kechinvv.analysis.Instrumentation
import org.kechinvv.analysis.JazzerRunner
import org.kechinvv.analysis.SootManager
import org.kechinvv.config.Configuration
import org.kechinvv.repository.LocalRepository
import org.kechinvv.utils.getPathForFuzz
import org.kechinvv.workflow.TestOkhttpWorkflow
import java.nio.file.Paths

fun main() {
    val configuration = Configuration()
    val classpath_jar =
        Paths.get("C:\\Users\\valer\\IdeaProjects\\libminer_test\\build\\libs\\libminer_test-1.0-SNAPSHOT.jar")

//
//    val classpath_jar2 =
//        Paths.get("C:\\Users\\valer\\IdeaProjects\\libminer_test\\build\\libs\\all-in-one-jar-1.0-SNAPSHOT.jar")
//
    val classpath = Paths.get("C:\\Users\\valer\\IdeaProjects\\libminer_test\\build\\classes\\java\\main")
//    val mvn_classpath = "C:\\Users\\valer\\IdeaProjects\\libminer_mvtest\\target\\classes"
//    val local = LocalRepository(Paths.get("C:\\Users\\valer\\IdeaProjects\\libminer_test"), configuration)
//    println(local.extractTracesFromLogs())
//    local.cleanLibMinerLogs()
//    local.build()
//    val i = Instrumentation().runAnalyze(classpath, setOf("secret"), classpath, false)
//    SootManager.instrumentLibCalls(classpath_jar, classpath_jar, true, setOf("java"))
//    val mvncntrl =
//        Paths.get("C:\\Users\\valer\\IdeaProjects\\LibMiner\\workdir\\okhttpmvn\\aca-java-1.0.0\\aca-java-1.0.0.jar")
//    SootManager.instrumentLibCalls(classpath_jar,  classpath_jar, true, setOf("java", "secret", "com.google.gson"))
//    SootManager.instrumentLibCalls(classpath,  classpath, false, setOf("java", "secret", "com.google.gson"))
//    local.cleanLibMinerLogs()
//    val entryPoints =
//        SootManager.getEntryPoints(classpath_jar)
//    println("Size: ${entryPoints.size}")
//    entryPoints.forEach {
//        JazzerRunner(100000, 300).run(listOf(classpath_jar.parent), it.getPathForFuzz(), classpath_jar.parent)
//    }
//    local.cleanLibMinerLogs()
//    Instrumentation().runAnalyze(classpath, "java", classpath, false)
//    local.runTests()
//    println(local.extractTracesFromLogs())
//    val target = Paths.get("./downloaded_reps")
//    target.createDirectory()
//    RemoteLib("com.atlassian.bamboo", "bamboo-specs-runner", "10.2.1").cloneTo(target)
    // TestOkhttpWorkflow().collectStaticTracesNew()
    TestOkhttpWorkflow().collectStaticTracesNew()
}