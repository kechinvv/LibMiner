package org.kechinvv.analysis

import kotlinx.serialization.json.Json
import org.kechinvv.entities.MethodData
import org.kechinvv.utils.foundLib
import soot.*
import soot.javaToJimple.DefaultLocalGenerator
import soot.jimple.Jimple
import soot.jimple.Stmt
import soot.jimple.StringConstant


class InstrumentationTransformer(val lib: String) : BodyTransformer() {
    /* some internal fields */


    override fun internalTransform(body: Body?, phase: String?, options: MutableMap<String, String>?) {
        val jimple = Jimple.v()
        val units = body?.units ?: return
        if (units.isEmpty()) return

        val lg = DefaultLocalGenerator(body)
        val stmtIt = units.snapshotIterator()

        //methods
        val identityHashCodeMethod = Scene.v()
            .getMethod("<java.lang.System: int identityHashCode(java.lang.Object)>")
        val toStringMethod = Scene.v().getMethod("<java.lang.Integer: java.lang.String toString(int)>")
        val concatMethod = Scene.v().getMethod("<java.lang.String: java.lang.String concat(java.lang.String)>")

        //create System.out
        val sysOutVar: Local = lg.generateLocal(RefType.v("java.io.PrintStream"))
        val sysOutField = Scene.v().getField("<java.lang.System: java.io.PrintStream out>")
        val sysOutAssignStmt =
            Jimple.v().newAssignStmt(sysOutVar, jimple.newStaticFieldRef(sysOutField.makeRef()))
        units.insertAfter(sysOutAssignStmt, units.first)


        while (stmtIt.hasNext()) {
            val stmt = stmtIt.next() as Stmt

            if (!stmt.containsInvokeExpr()) continue
            if (!stmt.invokeExpr.method.foundLib(lib)) continue


            val methodData = MethodData.fromSootMethod(stmt.invokeExpr.method)
            val generatedUnits: ArrayList<soot.Unit> = ArrayList()
            val prefixStr = StringConstant.v(
                String.format(
                    "[LibMiner] %s | iHash: ",
                    Json.encodeToString(methodData)
                )
            )

            // val a = prefix
            val prefixStrVar = lg.generateLocal(RefType.v("java.lang.String"))
            val assignPrefixStmt = jimple.newAssignStmt(prefixStrVar, prefixStr)
            generatedUnits.add(assignPrefixStmt)

            val printableVar = if (methodData.isStatic) {
                prefixStrVar
            } else {
                val analyzedObj = stmt.invokeExpr.useBoxes[0].value

                //Get identityHashCode
                val intHashCodeVar = lg.generateLocal(IntType.v())
                val getIdentityHashCodeInvoke =
                    jimple.newStaticInvokeExpr(identityHashCodeMethod.makeRef(), analyzedObj)
                val getIdentityHashCodeStmt = jimple.newAssignStmt(intHashCodeVar, getIdentityHashCodeInvoke)
                generatedUnits.add(getIdentityHashCodeStmt)

                //Assign string for printing
                val strHashCodeVar = lg.generateLocal(RefType.v("java.lang.String"))
                val intToStrInvoke = jimple.newStaticInvokeExpr(toStringMethod.makeRef(), intHashCodeVar)
                val strHashStmt = jimple.newAssignStmt(strHashCodeVar, intToStrInvoke)
                generatedUnits.add(strHashStmt)

                // printableVar = "info str ".concat(strHashCodeVar)
                val printableVar = lg.generateLocal(RefType.v("java.lang.String"))
                val concatInvoke = jimple.newVirtualInvokeExpr(prefixStrVar, concatMethod.makeRef(), strHashCodeVar)
                val assignPrintableStmt = jimple.newAssignStmt(printableVar, concatInvoke)
                generatedUnits.add(assignPrintableStmt)
                printableVar
            }

            // Create println method call and provide its parameter
            val printlnMethod = Scene.v().grabMethod("<java.io.PrintStream: void println(java.lang.String)>")
            val printlnMethodCallStmt = jimple
                .newInvokeStmt(jimple.newVirtualInvokeExpr(sysOutVar, printlnMethod.makeRef(), printableVar))
            generatedUnits.add(printlnMethodCallStmt)

            units.insertAfter(generatedUnits, stmt)
        }


    }
}