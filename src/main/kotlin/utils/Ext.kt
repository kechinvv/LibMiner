package org.kechinvv.utils

import org.kechinvv.entities.EntryFilter
import soot.SootMethod

fun SootMethod.foundLib(lib: String): Boolean {
    val declClass = this.declaringClass.toString()
    return declClass.startsWith("$lib.", true) || declClass.lowercase() == lib.lowercase()
}

fun SootMethod.isEntryPoint(filters: List<EntryFilter>): Boolean {
    if (this.isMain) return true
    filters.forEach { filter ->
        if (this.tags.intersect(filter.methodTags.toSet()).isNotEmpty()) return true
        if (this.declaringClass.tags.intersect(filter.classTags.toSet()).isNotEmpty()) return true
        if (filter.methodName != null) if (filter.methodName == this.name) return true
        if (filter.className != null) if (filter.className == this.declaringClass.name) return true
    }
    return false
}