package org.kechinvv.analysis

import kotlinx.serialization.json.Json
import org.kechinvv.entities.InvokeData
import org.kechinvv.entities.MethodData
import org.kechinvv.utils.foundLib
import soot.*
import soot.javaToJimple.DefaultLocalGenerator
import soot.jimple.*


class InstrumentationTransformer(val lib: String, val logPath: String) : BodyTransformer() {

    //methods


    override fun internalTransform(body: Body?, phase: String?, options: MutableMap<String, String>?) {
        val jimple = Jimple.v()
        val units = body?.units ?: return
        if (units.isEmpty()) return
        if (body.method.declaringClass.name == "LibMinerInstrumentationHelper") return

        val lg = DefaultLocalGenerator(body)
        val stmtIt = units.snapshotIterator()


        val logObj = Scene.v()
            .getMethod("<LibMinerInstrumentationHelper: void writeInvokeInfoObj(java.lang.String,java.lang.Object)>")
        val logStatic =
            Scene.v().getMethod("<LibMinerInstrumentationHelper: void writeInvokeInfoObj(java.lang.String)>")

        while (stmtIt.hasNext()) {
            val stmt = stmtIt.next() as Stmt
            if (!stmt.containsInvokeExpr()) continue
            if (!stmt.invokeExpr.method.foundLib(lib)) continue

            val generatedUnits: ArrayList<soot.Unit> = ArrayList()

            val methodData = MethodData.fromSootMethod(stmt.invokeExpr.method)
            val invokeDataStr = Json.encodeToString(InvokeData(methodData, "%d", "%d", "%s")) + "\n"
            val invokeDataStrConst = StringConstant.v(invokeDataStr)

            val metaVar = lg.generateLocal(RefType.v("java.lang.String"))
            val metaVarStmt = jimple.newAssignStmt(metaVar, invokeDataStrConst)
            generatedUnits.add(metaVarStmt)

            if (methodData.isStatic) {
                val callLogStatic =
                    jimple.newInvokeStmt(jimple.newStaticInvokeExpr(logStatic.makeRef(), metaVar))
                generatedUnits.add(callLogStatic)
            } else {
                val analyzedObj = stmt.invokeExpr.useBoxes[0].value

                val callLogObj =
                    jimple.newInvokeStmt(jimple.newStaticInvokeExpr(logObj.makeRef(), metaVar, analyzedObj))
                generatedUnits.add(callLogObj)
            }

            units.insertAfter(generatedUnits, stmt)
        }


    }
}