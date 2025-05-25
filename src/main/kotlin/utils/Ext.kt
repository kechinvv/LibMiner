package org.kechinvv.utils

import org.kechinvv.entities.EntryFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import soot.SootMethod
import soot.tagkit.AnnotationTag
import soot.tagkit.VisibilityAnnotationTag

fun SootMethod.foundLib(lib: String): Boolean {
    val declClass = this.declaringClass.toString()
    return declClass.startsWith("$lib.", true) || declClass.lowercase() == lib.lowercase()
}

fun SootMethod.isEntryPoint(filters: List<EntryFilter>): Boolean {
    if (this.isMain) return true
    filters.forEach { filter ->
        if (this.tags.fold(HashSet<AnnotationTag>()) { acc, it ->
                if (it is VisibilityAnnotationTag) acc.addAll(it.annotations)
                acc
            }.intersect(filter.methodAnnotation).isNotEmpty()) return true
        if (this.declaringClass.tags.fold(HashSet<AnnotationTag>()) { acc, it ->
                if (it is VisibilityAnnotationTag) acc.addAll(it.annotations)
                acc
            }.intersect(filter.classAnnotation).isNotEmpty()) return true
        if (filter.methodName != null) if (filter.methodName.matches(this.name)) return true
        if (filter.className != null) if (filter.className.matches(this.declaringClass.name)) return true
    }
    return false
}

fun <R : Any> R.logger(): Lazy<Logger> = lazy {
    return@lazy LoggerFactory
        .getLogger((if (this.javaClass.kotlin.isCompanion) this.javaClass.enclosingClass else this.javaClass).name)
}