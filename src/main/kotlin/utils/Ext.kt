package org.kechinvv.utils

import org.kechinvv.entities.EntryFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

fun <R : Any> R.logger(): Lazy<Logger> = lazy {
    return@lazy LoggerFactory
        .getLogger((if (this.javaClass.kotlin.isCompanion) this.javaClass.enclosingClass else this.javaClass).name)
}