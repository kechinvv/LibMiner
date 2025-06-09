package org.kechinvv.inference

import kotlinx.serialization.Serializable

@Serializable
data class Shift(var from: String, var to: String, var with: List<String>) {
    override fun toString(): String = "{from: ${from}, to: ${to}, with: ${with}}"

    fun withToLabel(): String = with.joinToString("\\n ")
}

@Serializable
data class State(val name: String, var type: StateType) {
    override fun toString(): String = "{name: ${name}, type: ${type}}"
}

@Serializable
data class Automaton(
    val klass: String,
    val shifts: Set<Shift>,
    val states: Set<State>,
)