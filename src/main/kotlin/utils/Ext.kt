package org.kechinvv.utils

import org.kechinvv.entities.EntryFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import soot.SootClass
import soot.SootMethod
import soot.tagkit.AbstractHost
import soot.tagkit.VisibilityAnnotationTag

fun SootMethod.getPathForFuzz(): String {
    return "${this.declaringClass}::${this.name}"
}

fun SootMethod.foundLib(lib: String): Boolean {
    val declClass = this.declaringClass.toString()
    return declClass.startsWith("$lib.", true) || declClass.lowercase() == lib.lowercase()
}

fun SootMethod.isEntryPoint(filters: List<EntryFilter>): Boolean {
    if (this.isMain) return true
    filters.forEach { f ->
        val match = (f.methodName == null || f.methodName.matches(this.name)) &&
                (f.className == null || f.className.matches(this.declaringClass.name)) &&
                (annotationCheck(f.methodAnnotation, this)) &&
                (annotationCheck(f.classAnnotation, this.declaringClass)) &&
                (kindCheck(f.kind, this)) &&
                (f.returnType == null || this.returnType.toString() == f.returnType) &&
                (checkArgs(f.args, this)) &&
                (methodModifiersCheck(f.methodModifiers, this)) &&
                (classModifiersCheck(f.classModifiers, this.declaringClass))
        if (!match) return false
    }

    return false
}

private fun kindCheck(kind: String?, method: SootMethod): Boolean {
    if (kind == null) return true
    when (kind) {
        "init" -> return method.isConstructor
        "clinit" -> return method.isStaticInitializer
        "method" -> return !method.isConstructor && !method.isStaticInitializer
    }
    return false
}

private fun annotationCheck(annotations: Set<String>?, target: AbstractHost): Boolean {
    if (annotations == null) return true
    return (target.getTag("VisibilityAnnotationTag") as VisibilityAnnotationTag).annotations.map { it.name }
        .containsAll(annotations)
}

private fun checkArgs(args: List<String>?, method: SootMethod): Boolean {
    if (args == null) return true
    return args == method.parameterTypes.map { it.toString() }
}

private fun methodModifiersCheck(modifiers: Set<String>?, target: SootMethod): Boolean {
    if (modifiers == null) return true
    val methodModifiers = HashSet<String>()
    if (target.isPublic) methodModifiers.add("public")
    if (target.isProtected) methodModifiers.add("protected")
    if (target.isPrivate) methodModifiers.add("private")
    if (target.isStatic) methodModifiers.add("static")
    if (target.isFinal) methodModifiers.add("final")
    if (target.isSynchronized) methodModifiers.add("synchronized")
    if (target.isNative) methodModifiers.add("native")
    return methodModifiers == modifiers
}

private fun classModifiersCheck(modifiers: Set<String>?, target: SootClass): Boolean {
    if (modifiers == null) return true
    val classModifiers = HashSet<String>()
    if (target.isPublic) classModifiers.add("public")
    if (target.isProtected) classModifiers.add("protected")
    if (target.isPrivate) classModifiers.add("private")
    if (target.isStatic) classModifiers.add("static")
    if (target.isFinal) classModifiers.add("final")
    if (target.isSynchronized) classModifiers.add("synchronized")
    if (target.isEnum) classModifiers.add("enum")
    return classModifiers == modifiers
}

fun <R : Any> R.logger(): Lazy<Logger> = lazy {
    return@lazy LoggerFactory
        .getLogger((if (this.javaClass.kotlin.isCompanion) this.javaClass.enclosingClass else this.javaClass).name)
}