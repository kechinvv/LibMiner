package org.kechinvv.analysis

import soot.*
import soot.javaToJimple.DefaultLocalGenerator
import soot.jimple.Jimple
import soot.jimple.Stmt
import soot.jimple.StringConstant


class InstrumentationTransformer(val lib: String) : BodyTransformer() {
    /* some internal fields */


    override fun internalTransform(body: Body?, phase: String?, options: MutableMap<String, String>?) {
        val units = body?.units ?: return
        val lg = DefaultLocalGenerator(body)
        val stmtIt = units.snapshotIterator()
        //Scene.v().loadClassAndSupport("java.lang.System")

        //methods
        val identityHashCodeMethod = Scene.v()
            .getMethod("<java.lang.System: int identityHashCode(java.lang.Object)>")
        val toStringMethod = Scene.v().getMethod("<java.lang.Integer: java.lang.String toString(int)>")
        val concatMethod = Scene.v().getMethod("<java.lang.String: java.lang.String concat(java.lang.String)>")

        val jimple = Jimple.v()

        while (stmtIt.hasNext()) {
            val stmt = stmtIt.next() as Stmt

            if (!stmt.containsInvokeExpr()) {
                continue;
            }

            val generatedUnits: ArrayList<soot.Unit> = ArrayList()

            if (stmt.invokeExpr.useBoxes.size == 0) {
                println("Statement with 0 box = $stmt")
                continue
            }
            val analyzedObj = stmt.invokeExpr.useBoxes[0].value

            val prefixStr = StringConstant.v(
                String.format(
                    "[LibMiner] stmt: %s, base: %s, iHash: ",
                    stmt.toString(),
                    analyzedObj.toString()
                )
            )
            val prefixStrVar = lg.generateLocal(RefType.v("java.lang.String"))
            val assignPrefixStmt = jimple.newAssignStmt(prefixStrVar, prefixStr)
            generatedUnits.add(assignPrefixStmt)

            //Get identityHashCode
            val intHashCodeVar = lg.generateLocal(IntType.v())
            val getIdentityHashCodeInvoke = jimple.newStaticInvokeExpr(identityHashCodeMethod.makeRef(), analyzedObj)
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
            val assignConcatStmt = jimple.newAssignStmt(printableVar, concatInvoke)
            generatedUnits.add(assignConcatStmt)

            //create System.out
            val sysOutVar: Local = lg.generateLocal(RefType.v("java.io.PrintStream"))
            val sysOutField = Scene.v().getField("<java.lang.System: java.io.PrintStream out>")
            val sysOutAssignStmt =
                Jimple.v().newAssignStmt(sysOutVar, jimple.newStaticFieldRef(sysOutField.makeRef()))
            generatedUnits.add(sysOutAssignStmt)


            // Create println method call and provide its parameter
            val printlnMethod = Scene.v().grabMethod("<java.io.PrintStream: void println(java.lang.String)>")
            val printlnMethodCallStmt = Jimple.v()
                .newInvokeStmt(Jimple.v().newVirtualInvokeExpr(sysOutVar, printlnMethod.makeRef(), printableVar))
            generatedUnits.add(printlnMethodCallStmt)

            units.insertAfter(generatedUnits, stmt)
        }


    }
}