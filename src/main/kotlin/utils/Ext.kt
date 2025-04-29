package org.kechinvv.utils

import soot.SootMethod

fun SootMethod.foundLib(lib: String): Boolean {
    val declClass = this.declaringClass.toString()
    return declClass.startsWith("$lib.", true) || declClass.lowercase() == lib.lowercase()
}

fun SootMethod.isEntryPoint() : Boolean {
    if (this.isMain) return true

    return false
}