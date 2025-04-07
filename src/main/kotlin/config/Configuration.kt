package org.kechinvv.config

import config.TraceNode
import kotlinx.serialization.Serializable

@Serializable
data class Configuration(
    val libName: String,
    val ghToken: String,

    val mavenPath: String,
    val gradlePath: String,
    val gradleVersion: String,

    val workdir: String,

    val kAlg: Int,
    val goal: Int,

    val allProj: Boolean,

    val traversJumps: Int,
    val traversDepth: Int,
    val traceLimit: Int,
    val traceNode: TraceNode,
    val unionEnd: Boolean,
    val toJson: Boolean,

    val saveDb: Boolean
)