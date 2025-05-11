package org.kechinvv.storage

import config.TraceNode
import org.kechinvv.entities.MethodData
import org.ktorm.database.Database
import org.ktorm.dsl.insert
import org.ktorm.support.sqlite.SQLiteDialect
import org.ktorm.support.sqlite.insertOrUpdate
import soot.SootMethod

class Storage(dbName: String) {

    var database = Database.connect(
        "jdbc:sqlite:$dbName",
        user = "root",
        password = "root",
        dialect = SQLiteDialect()
    )

    init {
        database.useConnection { conn ->
            conn.createStatement().use { statement ->
                val script = javaClass.classLoader.getResource("create_tables.sql")?.readText()
                    ?: throw Exception("could not find init script")
                statement.executeUpdate(script)
            }
        }
    }

    fun saveMethod(method: SootMethod, detail: TraceNode) {
        val methodData = MethodData.fromSootMethod(method)
        val klass = method.declaringClass.toString()
        val name = if (detail == TraceNode.NAME) methodData.name else methodData.getSignature()
        database.insertOrUpdate(MethodEntity) {
            set(it.name, name)
            set(it.klass, klass)
            onConflict { doNothing() }
        }
    }

    fun saveTrace(inputData: String, inputClass: String, inputCount: Int = 1) {

    }

    fun getTraceById(id: Int): String {
//        val stmt = conn.prepareStatement("select json_data from sequences where id=?")
//        stmt.setInt(1, id)
//        val res = stmt.executeQuery()
//        if (res.next()) {
//            return res.getString("json_data")
//        } else return null
        return ""
    }

    fun getClasses(): HashSet<String> {
//        val classes = hashSetOf<String>()
//        val stmt = conn.createStatement()
//        val res = stmt.executeQuery("select distinct class from sequences")
//        while (res.next()) {
//            val className = res.getString("class")
//            classes.add(className)
//        }
//
//        return classes
        return HashSet()
    }

    fun getMethodsForClass(inputClass: String): HashSet<String> {
//        val methodNames = HashSet<String>()
//        val stmt = conn.prepareStatement("select method_name from methods where class=?")
//        stmt.setString(1, inputClass)
//        val res = stmt.executeQuery()
//        while (res.next()) {
//            val name = res.getString("method_name")
//            methodNames.add(name)
//        }
//        return methodNames
        return HashSet()
    }

    fun getTracesIdForClass(inputClass: String): HashSet<Int> {
//        val idTraces = HashSet<Int>()
//        val stmt = conn.prepareStatement("select id from sequences where class=?")
//        stmt.setString(1, inputClass)
//        val res = stmt.executeQuery()
//        while (res.next()) {
//            val id = res.getInt("id")
//            idTraces.add(id)
//        }
//
//        return idTraces
        return HashSet()
    }

}