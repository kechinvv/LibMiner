package org.kechinvv.analysis

import com.google.gson.GsonBuilder
import kotlinx.serialization.json.Json
import org.kechinvv.config.Configuration
import org.kechinvv.entities.MethodData
import org.kechinvv.storage.Storage
import org.kechinvv.utils.foundLib
import org.kechinvv.utils.isEntryPoint
import soot.*
import soot.Unit
import soot.jimple.InvokeExpr
import soot.jimple.internal.AbstractStmt
import soot.jimple.internal.JAssignStmt
import soot.jimple.internal.JInvokeStmt
import soot.jimple.spark.pag.PAG
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG

class SequenceCollectorTransformer(val lib: String, val storage: Storage, val configuration: Configuration) :
    SceneTransformer() {
    lateinit var icfg: JimpleBasedInterproceduralCFG
    lateinit var analysis: PAG
    private var counter = 0
    private var stop = false

    override fun internalTransform(phaseName: String?, options: MutableMap<String, String>?) {
        val entryPoints = mutableListOf<SootMethod>()
        Scene.v().applicationClasses.forEach { klass ->
            klass.methods.forEach {
                if (it.isEntryPoint()) entryPoints.add(it)
            }
        }
        println("Entry points size: ${entryPoints.size}")
        Scene.v().entryPoints = entryPoints
        icfg = JimpleBasedInterproceduralCFG()
        //icfg.setIncludePhantomCallees(true)
        analysis = Scene.v().pointsToAnalysis as PAG

        entryPoints.forEach { mainMethod ->
            val startPoints = icfg.getStartPointsOf(mainMethod)
            println("Entry Points are: ")
            println(startPoints)

            stop = false
            counter = 0

            startPoints.forEach { startPoint ->
                graphTraverseLib(startPoint)
            }
            println("Total traces analyzed = $counter")
        }
    }

    fun graphTraverseLib(
        startPoint: Unit,
        ttl: Int = configuration.traversJumps,
        isMethod: Boolean = false,
        extracted: HashMap<String, MutableList<MutableList<InvokeExpr>>> = HashMap(),
        continueStack: ArrayDeque<Pair<Unit, Boolean>> = ArrayDeque(),
        depth: Int = configuration.traversDepth
    ) {
        val currentSuccessors = icfg.getSuccsOf(startPoint)
        if (currentSuccessors.size == 0 || ttl <= 0 || depth == 0) {
            if (ttl <= 0 || !isMethod) {
                counter++
                if (counter % 50000 == 0) println("Traces already analyzed... = $counter")
                if (counter == configuration.traceLimit) stop = true
                save(extracted)
            } else {
                val succInfo = continueStack.removeLast()
                graphTraverseLib(succInfo.first, ttl - 1, succInfo.second, extracted, continueStack, depth + 1)
                continueStack.add(succInfo)
            }
        } else {
            for (succ in currentSuccessors) {
                var method: SootMethod? = null
                var continueAdded = false
                var klass: String? = null
                var addedIndex: List<Int>? = null
                try {
                    if (stop) return
                    if (succ is JInvokeStmt || succ is JAssignStmt) {
                        succ as AbstractStmt
                        if (succ.invokeExpr.method.declaringClass in Scene.v().applicationClasses)
                            method = succ.invokeExpr.method
                        if (method?.foundLib(lib) == true) {
                            storage.saveMethod(succ.invokeExpr.method, configuration.traceNode)
                            val methodLib = succ.invokeExpr.method
                            klass = if (methodLib.isStatic) "${methodLib.declaringClass}__s"
                            else methodLib.declaringClass.toString()

                            if (extracted[klass] == null) extracted[klass] = mutableListOf()
                            addedIndex = fillExtracted(succ.invokeExpr, extracted[klass]!!)
                        }
                    }
                } catch (_: Exception) {
                }
                if (method != null && method.declaringClass in Scene.v().applicationClasses) {
                    continueStack.add(Pair(succ, isMethod))
                    continueAdded = true
                    icfg.getStartPointsOf(method).forEach { methodStart ->
                        graphTraverseLib(methodStart, ttl - 1, true, extracted, continueStack, depth - 1)
                    }
                } else graphTraverseLib(succ, ttl - 1, isMethod, extracted, continueStack, depth)

                if (addedIndex != null) confiscate(addedIndex, extracted[klass]!!)
                if (continueAdded) continueStack.removeLast()
            }
        }
    }


    private fun fillExtracted(invoke: InvokeExpr, extractedKlass: MutableList<MutableList<InvokeExpr>>): List<Int> {
        return if (invoke.method.isStatic) {
            if (extractedKlass.size != 0) extractedKlass[0].add(invoke)
            else extractedKlass.add(mutableListOf(invoke))
            listOf(0)
        } else {
            defaultExtracting(invoke, extractedKlass)
        }
    }

    private fun confiscate(indexes: List<Int>, extractedKlass: MutableList<MutableList<InvokeExpr>>) {
        indexes.forEach { index ->
            extractedKlass[index].removeLast()
            if (extractedKlass[index].isEmpty()) extractedKlass.removeAt(index)
        }
    }

    private fun save(extracted: HashMap<String, MutableList<MutableList<InvokeExpr>>>) {
        extracted.forEach { (key, value) ->
            value.forEach inner@{ trace ->
                if (trace.size == 0) return@inner
                var tempTrace = mutableListOf<InvokeExpr>()
                trace.forEach { invokeGlob ->
                    if (invokeGlob.method.name == "<init>" || invokeGlob == trace.last()) {
                        if (invokeGlob.method.name != "<init>") tempTrace.add(invokeGlob)
                        if (tempTrace.isNotEmpty()) {
                            val jsonData = GsonBuilder().disableHtmlEscaping().create().toJson(tempTrace.map { invoke ->
                                val methodData = MethodData.fromSootMethod(invoke.method)
                                Json.encodeToString(methodData)
                            })
                            val inputClass = if (key.endsWith("__s")) key.dropLast(3) else key
                            storage.saveTrace(jsonData!!, inputClass)
                        }
                        tempTrace = mutableListOf(invokeGlob)
                    } else tempTrace.add(invokeGlob)
                }
            }
        }
    }


    private fun defaultExtracting(invoke: InvokeExpr, extractedKlass: MutableList<MutableList<InvokeExpr>>): List<Int> {
        val obj1PT = getPointsToSet(invoke)
        val indexes = mutableListOf<Int>()
        var added = false
        extractedKlass.forEachIndexed { index, it ->
            val obj2PT = getPointsToSet(it.last())
            if (obj1PT.hasNonEmptyIntersection(obj2PT)) {
                it.add(invoke)
                indexes.add(index)
                added = true
            }
        }
        return if (!added) {
            extractedKlass.add(mutableListOf(invoke))
            listOf(extractedKlass.lastIndex)
        } else indexes
    }


    private fun getPointsToSet(inv: InvokeExpr): PointsToSet {
        return analysis.reachingObjects(inv.useBoxes[0].value as Local)
    }


}