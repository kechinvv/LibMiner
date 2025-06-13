package org.kechinvv.analysis

import com.charleskorn.kaml.Yaml
import org.kechinvv.config.Configuration
import org.kechinvv.entities.EntryFilter
import org.kechinvv.entities.MethodData
import org.kechinvv.storage.Storage
import org.kechinvv.utils.ExtractMethod
import org.kechinvv.utils.foundLib
import org.kechinvv.utils.logger
import soot.*
import soot.Unit
import soot.jimple.internal.AbstractStmt
import soot.jimple.internal.JAssignStmt
import soot.jimple.spark.pag.PAG
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.extension
import kotlin.io.path.readText

class SequenceCollectorTransformer(val storage: Storage, val configuration: Configuration) :
    SceneTransformer() {
    lateinit var icfg: JimpleBasedInterproceduralCFG
    lateinit var analysis: PAG
    private var counter = 0
    private var stop = false
    private val filters = Files.walk(Paths.get("entry_points_filters")).filter { it.extension == "yaml" }
        .map { Yaml.default.decodeFromString(EntryFilter.serializer(), it.readText()) }.collect(Collectors.toSet())

    companion object {
        val LOG by logger()
    }

    override fun internalTransform(phaseName: String?, options: MutableMap<String, String>?) {
        LOG.info("internalTransform")

        icfg = JimpleBasedInterproceduralCFG()
        icfg.setIncludePhantomCallees(true)

        analysis = Scene.v().pointsToAnalysis as PAG

        Scene.v().entryPoints.forEach { entryMethod ->
            val startPoints = icfg.getStartPointsOf(entryMethod)
            LOG.info("Starting with : {}", startPoints)
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
        extracted: HashMap<String, MutableList<MutableList<AbstractStmt>>> = HashMap(),
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
                    if (succ is AbstractStmt) {
                        method = succ.invokeExpr.method
                        if (method?.foundLib(configuration.targetLibExtractingUnit) == true) {
                            klass = method.declaringClass.toString()
                            if (extracted[klass] == null) extracted[klass] = mutableListOf()
                            indexesOfChangedTraces = saveInvokeToTrace(succ, extracted[klass]!!)
                        }
                    }
                } catch (_: Exception) {
                }
                if (method != null && depth > 0 && method.declaringClass.isApplicationClass) {
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
    private fun saveInvokeToTrace(
        successor: AbstractStmt,
        tracesForClass: MutableList<MutableList<AbstractStmt>>
    ): List<Int> {
        val obj1PT = getPointsToSet(successor)
        val indexes = mutableListOf<Int>()
        var added = false
        tracesForClass.forEachIndexed { index, trace ->
            val obj2PT = getPointsToSet(trace.last())
            if (obj1PT.hasNonEmptyIntersection(obj2PT)) {
                trace.add(successor)
                added = indexes.add(index)
            }
        }
        return if (!added) {
            tracesForClass.add(mutableListOf(successor))
            listOf(tracesForClass.lastIndex)
        } else indexes
    }


    private fun resetTraces(indexesOfChangedTraces: List<Int>, extractedKlass: MutableList<MutableList<AbstractStmt>>) {
        indexesOfChangedTraces.forEach { index ->
            extractedKlass[index].removeLast()
            if (extractedKlass[index].isEmpty()) extractedKlass.removeAt(index)
        }
    }

    private fun save(extracted: HashMap<String, MutableList<MutableList<AbstractStmt>>>) {
        extracted.forEach { (klass, tracesInvokeExpr) ->
            tracesInvokeExpr.forEach inner@{ traceInvokeExpr ->
                if (traceInvokeExpr.size == 0) return@inner
                val traceMethodData = traceInvokeExpr.map { MethodData.fromSootMethod(it.invokeExpr.method) }
                storage.saveTrace(traceMethodData, klass, ExtractMethod.STATIC)
            }
        }
    }

    private fun getPointsToSet(successor: AbstractStmt): PointsToSet {
        val resObj =
            if (successor is JAssignStmt && successor.invokeExpr.method.isStatic) successor.leftOp
            else successor.invokeExpr.useBoxes[0].value
        return this.analysis.reachingObjects(resObj as Local)
    }

}





