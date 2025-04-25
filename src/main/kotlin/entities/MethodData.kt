package org.kechinvv.entities

import kotlinx.serialization.Serializable
import soot.SootMethod


@Serializable
data class MethodData(val name: String, val args: List<String>, val isStatic: Boolean, val klass: String) {
    companion object MethodParser {
        fun fromSootMethod(method: SootMethod): MethodData {
            val name = method.name
            val args = mutableListOf<String>()
            method.parameterTypes.forEach { type ->
                val finType = type.defaultFinalType.toString()
                args.add(finType)
            }
            return MethodData(name, args, method.isStatic, method.declaringClass.toString())
        }
    }

    fun getSignature(): String {
        return "${name}(${args.joinToString(", ")})"
    }
}