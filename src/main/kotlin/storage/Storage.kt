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

}