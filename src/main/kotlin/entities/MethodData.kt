package org.kechinvv.entities

import kotlinx.serialization.Serializable
import soot.SootMethod


@Serializable
data class MethodData(val name: String, val args: List<String>) {
    companion object MethodParser {
        fun fromSootMethod(method: SootMethod): MethodData {
            val name = if (method.name == "<init>") "constructor" else method.name
            val args = mutableListOf<String>()
            method.parameterTypes.forEach { type ->
                val finType = type.defaultFinalType.toString()
                args.add(finType)
            }
            return MethodData(name, args)
        }
    }

    fun getSignature(): String {
        return "${name}(${args.joinToString(", ")})"
    }
}