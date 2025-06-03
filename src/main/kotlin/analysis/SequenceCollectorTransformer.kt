package org.kechinvv.analysis

import kotlinx.serialization.json.Json
import org.kechinvv.config.Configuration
import org.kechinvv.entities.MethodData
import org.kechinvv.storage.Storage
import org.kechinvv.utils.foundLib
import org.kechinvv.utils.isEntryPoint
import org.kechinvv.utils.logger
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

    companion object {
        val LOG by logger()
    }

    override fun internalTransform(phaseName: String?, options: MutableMap<String, String>?) {
        Scene.v().entryPoints = mutableListOf<SootMethod>()
        collectEntryPointsTo(Scene.v().entryPoints)

        icfg = JimpleBasedInterproceduralCFG()
        //icfg.setIncludePhantomCallees(true)
        analysis = Scene.v().pointsToAnalysis as PAG

        Scene.v().entryPoints.forEach { mainMethod ->
            val startPoints = icfg.getStartPointsOf(mainMethod)
            LOG.debug("Starting {}", startPoints)
            stop = false
            counter = 0

            startPoints.forEach { startPoint -> graphTraverseLib(startPoint) }
            LOG.info("Total traces analyzed = {}", counter)
        }
    }

    fun graphTraverseLib(
        startPoint: Unit,
        ttl: Int = configuration.traversLength,
        isMainMethod: Boolean = true,
        extracted: HashMap<String, MutableList<MutableList<InvokeExpr>>> = HashMap(),
        continueStack: ArrayDeque<Pair<Unit, Boolean>> = ArrayDeque(),
        depth: Int = configuration.traversDepth
    ) {
        val currentSuccessors = icfg.getSuccsOf(startPoint)
        if (currentSuccessors.size == 0 || ttl <= 0) {
            if (ttl <= 0 || isMainMethod) {
                counter++
                if (counter % 50000 == 0) LOG.info("Traces already analyzed... = {}", counter)
                if (counter == configuration.traceCount) stop = true
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
                var indexesOfChangedTraces: List<Int>? = null
                try {
                    if (stop) return
                    if (succ is JInvokeStmt || succ is JAssignStmt) {
                        succ as AbstractStmt
                        if (succ.invokeExpr.method.declaringClass in Scene.v().applicationClasses)
                            method = succ.invokeExpr.method
                        if (method?.foundLib(lib) == true) {
                            val methodLib = succ.invokeExpr.method
                            klass = methodLib.declaringClass.toString()
                            if (extracted[klass] == null) extracted[klass] = mutableListOf()
                            indexesOfChangedTraces = saveInvokeToTrace(succ.invokeExpr, extracted[klass]!!)
                        }
                    }
                } catch (_: Exception) {
                }
                if (method != null && depth > 0) {
                    continueAdded = continueStack.add(Pair(succ, isMainMethod))
                    icfg.getStartPointsOf(method).forEach { methodStart ->
                        graphTraverseLib(methodStart, ttl - 1, false, extracted, continueStack, depth - 1)
                    }
                } else graphTraverseLib(succ, ttl - 1, isMainMethod, extracted, continueStack, depth)

                if (indexesOfChangedTraces != null) resetTraces(indexesOfChangedTraces, extracted[klass]!!)
                if (continueAdded) continueStack.removeLast()
            }
        }
    }

    // return indexes for pop after trace finish
    private fun saveInvokeToTrace(invoke: InvokeExpr, tracesForClass: MutableList<MutableList<InvokeExpr>>): List<Int> {
        //static traces reserved place
        if (tracesForClass.size == 0) tracesForClass.add(mutableListOf())

        return if (invoke.method.isStatic) {
            tracesForClass[0].add(invoke)
            listOf(0)
        } else {
            defaultSaveInvokeToTrace(invoke, tracesForClass)
        }
    }

    // return indexes for pop after trace finish
    private fun defaultSaveInvokeToTrace(
        invoke: InvokeExpr,
        extractedKlass: MutableList<MutableList<InvokeExpr>>
    ): List<Int> {
        val obj1PT = getPointsToSet(invoke)
        val indexes = mutableListOf<Int>()
        var added = false
        extractedKlass.forEachIndexed { index, it ->
            val obj2PT = getPointsToSet(it.last())
            if (obj1PT.hasNonEmptyIntersection(obj2PT)) {
                it.add(invoke)
                added = indexes.add(index)
            }
        }
        return if (!added) {
            extractedKlass.add(mutableListOf(invoke))
            listOf(extractedKlass.lastIndex)
        } else indexes
    }


    private fun resetTraces(indexesOfChangedTraces: List<Int>, extractedKlass: MutableList<MutableList<InvokeExpr>>) {
        indexesOfChangedTraces.forEach { index ->
            extractedKlass[index].removeLast()
            if (extractedKlass[index].isEmpty()) extractedKlass.removeAt(index)
        }
    }

    private fun save(extracted: HashMap<String, MutableList<MutableList<InvokeExpr>>>) {
        extracted.forEach { (klass, tracesInvokeExpr) ->
            tracesInvokeExpr.forEach inner@{ traceInvokeExpr ->
                if (traceInvokeExpr.size == 0) return@inner
                val traceMethodData = traceInvokeExpr.map { MethodData.fromSootMethod(it.method) }
                storage.saveTrace(Json.encodeToString(traceMethodData), klass, traceInvokeExpr.first().method.isStatic)
                //save possible methods for MINT
                //traceMethodData.distinct().forEach(storage::saveMethod)
            }
        }
    }

    private fun getPointsToSet(inv: InvokeExpr): PointsToSet {
        return this.analysis.reachingObjects(inv.useBoxes[0].value as Local)
    }

    private fun collectEntryPointsTo(entryPoints: MutableCollection<SootMethod>) {
        Scene.v().applicationClasses.forEach { klass ->
            klass.methods.forEach {
                if (it.isEntryPoint(emptyList())) entryPoints.add(it)
            }
        }
        LOG.info("Entry points size: ${entryPoints.size}")
    }
}





