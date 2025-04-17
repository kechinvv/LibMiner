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

        val lg = DefaultLocalGenerator(body)
        val stmtIt = units.snapshotIterator()


//        val iHashCodeMethod = Scene.v()
//            .getMethod("<java.lang.System: int identityHashCode(java.lang.Object)>")
//        val concatMethod = Scene.v().getMethod("<java.lang.String: java.lang.String concat(java.lang.String)>")
//        val currentThreadMethod = Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>")
//        val getNameMethod = Scene.v().getMethod("<java.lang.Thread: java.lang.String getName()>")
//        val pathsGet =
//            Scene.v().getMethod("<java.nio.file.Paths: java.nio.file.Path get(java.lang.String,java.lang.String[])>")
//        val formatMethod =
//            Scene.v().getMethod("<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>");
//        val getBytes = Scene.v().getMethod("<java.lang.String: byte[] getBytes()>")
//        val writeMethod = Scene.v()
//            .getMethod("<java.nio.file.Files: java.nio.file.Path write(java.nio.file.Path,byte[],java.nio.file.OpenOption[])>")


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
            val invokeDataStr = Json.encodeToString(InvokeData(methodData, "%d")) + "\n"
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
                val objVar = lg.generateLocal(RefType.v("java.lang.Object"))
                val assignObjVar = jimple.newAssignStmt(objVar, analyzedObj)
                generatedUnits.add(assignObjVar)
                val callLogObj =
                    jimple.newInvokeStmt(jimple.newStaticInvokeExpr(logObj.makeRef(), metaVar, objVar))
                generatedUnits.add(callLogObj)
            }

//            val threadLocal = lg.generateLocal(RefType.v("java.lang.Thread"))
//            val threadNameLocal = lg.generateLocal(RefType.v("java.lang.String"))
//
//            //todo: create on each block
//            val getCurThreadStmt = Jimple.v().newAssignStmt(
//                threadLocal,
//                Jimple.v().newStaticInvokeExpr(currentThreadMethod.makeRef())
//            )
//            generatedUnits.add(getCurThreadStmt)
//
//            val getThreadNameStmt = Jimple.v().newAssignStmt(
//                threadNameLocal,
//                Jimple.v().newVirtualInvokeExpr(threadLocal, getNameMethod.makeRef())
//            )
//            generatedUnits.add(getThreadNameStmt)
//
//            //filename = thread + _libminer.log
//            val fileName = lg.generateLocal(RefType.v("java.lang.String"));
//            val fileNameConcatInvoke =
//                jimple.newVirtualInvokeExpr(threadNameLocal, concatMethod.makeRef(), StringConstant.v("_libminer.log"))
//            val fileNameConcatAssignment = jimple.newAssignStmt(fileName, fileNameConcatInvoke)
//            generatedUnits.add(fileNameConcatAssignment)
//
//            val stringArrayType = ArrayType.v(RefType.v("java.lang.String"), 1)
//            val stringArray = lg.generateLocal(stringArrayType)
//
//            val newArrayExprFilename = jimple.newNewArrayExpr(RefType.v("java.lang.String"), IntConstant.v(1))
//            val assignArrayFilenameStmt = jimple.newAssignStmt(stringArray, newArrayExprFilename)
//            generatedUnits.add(assignArrayFilenameStmt)
//
//            val arrayRef = jimple.newArrayRef(stringArray, IntConstant.v(0))
//            val fillArrayStmt = jimple.newAssignStmt(arrayRef, fileName)
//            generatedUnits.add(fillArrayStmt)
//
//            //path = Paths.get(path_dir, filename)
//            val pathLocal = lg.generateLocal(RefType.v("java.nio.file.Path"))
//            val pathsGetInvoke = jimple.newStaticInvokeExpr(pathsGet.makeRef(), StringConstant.v(logPath), stringArray)
//            val pathLocalAssignStmt = jimple.newAssignStmt(pathLocal, pathsGetInvoke)
//            generatedUnits.add(pathLocalAssignStmt)
//
//
//            val printableVar = lg.generateLocal(RefType.v("java.lang.String"))
//
//            val assignPrintable = jimple.newAssignStmt(printableVar, invokeDataStrConst)
//            generatedUnits.add(assignPrintable)
//
//            if (methodData.isStatic) {
//                val assignPrintable = jimple.newAssignStmt(printableVar, invokeDataStrConst)
//                generatedUnits.add(assignPrintable)
//            } else {
//                val analyzedObj = stmt.invokeExpr.useBoxes[0].value
//
//                //Get identityHashCode
//                val intHashCodeVar = lg.generateLocal(IntType.v())
//                val getIdentityHashCodeInvoke = jimple.newStaticInvokeExpr(iHashCodeMethod.makeRef(), analyzedObj)
//                val getIdentityHashCodeStmt = jimple.newAssignStmt(intHashCodeVar, getIdentityHashCodeInvoke)
//                generatedUnits.add(getIdentityHashCodeStmt)
//
//                val formatInvoke =
//                    jimple.newStaticInvokeExpr(formatMethod.makeRef(), invokeDataStrConst, intHashCodeVar)
//                val printableAssignmentStmt = jimple.newAssignStmt(printableVar, formatInvoke)
//                generatedUnits.add(printableAssignmentStmt)
//            }
//
//            val byteArr = lg.generateLocal(ArrayType.v(ByteType.v(), 1))
//            val toByteArrInvoke = jimple.newVirtualInvokeExpr(printableVar, getBytes.makeRef())
//            val byteArrayAssignmentStmt = jimple.newAssignStmt(
//                byteArr,
//                toByteArrInvoke
//            )
//            generatedUnits.add(byteArrayAssignmentStmt)
//
//            val openOption = Scene.v().getSootClass("java.nio.file.StandardOpenOption")
//            val appendRef = Jimple.v().newStaticFieldRef(
//                openOption.getFieldByName("APPEND").makeRef()
//            )
//            val createRef = Jimple.v().newStaticFieldRef(
//                openOption.getFieldByName("CREATE").makeRef()
//            )
//
//            val appendOptionLocal = lg.generateLocal(RefType.v("java.nio.file.StandardOpenOption"))
//            generatedUnits.add(Jimple.v().newAssignStmt(appendOptionLocal, appendRef))
//            val createOptionLocal = lg.generateLocal(RefType.v("java.nio.file.StandardOpenOption"))
//            generatedUnits.add(Jimple.v().newAssignStmt(createOptionLocal, createRef))
//
//            val openOptionArray = lg.generateLocal(ArrayType.v(RefType.v("java.nio.file.OpenOption"), 1))
//
//            val newArrayExpr = jimple.newNewArrayExpr(RefType.v("java.nio.file.OpenOption"), IntConstant.v(2))
//            val assignArray = jimple.newAssignStmt(openOptionArray, newArrayExpr)
//            generatedUnits.add(assignArray)
//
//            // Fill array[0] = CREATE
//            val arrayRef0 = Jimple.v().newArrayRef(openOptionArray, IntConstant.v(0))
//            val assignCreate = Jimple.v().newAssignStmt(arrayRef0, createOptionLocal)
//            generatedUnits.add(assignCreate)
//
//            // Fill array[1] = APPEND
//            val arrayRef1 = Jimple.v().newArrayRef(openOptionArray, IntConstant.v(1))
//            val assignAppend = Jimple.v().newAssignStmt(arrayRef1, appendOptionLocal)
//            generatedUnits.add(assignAppend)
//
//            val appendText = jimple.newInvokeStmt(jimple.newStaticInvokeExpr(
//                writeMethod.makeRef(),
//                pathLocal,
//                byteArr,
//                openOptionArray
//            ))
//            generatedUnits.add(appendText)

            units.insertAfter(generatedUnits, stmt)
        }


    }
}