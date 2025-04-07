package org.kechinvv

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import org.kechinvv.config.Configuration
import java.io.FileInputStream

fun main() {
    val configuration =
        Yaml.default.decodeFromStream(Configuration.serializer(), FileInputStream("config.yaml"))
    println(configuration.kAlg)
//    val target = Paths.get("./downloaded_reps")
//    target.createDirectory()
//    RemoteLib("com.atlassian.bamboo", "bamboo-specs-runner", "10.2.1").cloneTo(target)
}