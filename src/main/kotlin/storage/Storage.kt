package org.kechinvv.storage

import kotlinx.serialization.json.Json
import org.kechinvv.entities.MethodData
import org.kechinvv.holders.TraceHolder
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.support.sqlite.SQLiteDialect
import org.ktorm.support.sqlite.insertOrUpdate
import java.io.FileNotFoundException

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
                    ?: throw FileNotFoundException("could not find init script")
                statement.executeUpdate(script)
            }
        }
    }

    fun saveMethod(method: MethodData) {
        database.insertOrUpdate(MethodEntity) {
            set(it.name, method.name)
            set(it.args, Json.encodeToString(method.args))
            set(it.returnType, method.returnType)
            set(it.klass, method.klass)
            set(it.isStatic, method.isStatic)
            onConflict { doNothing() }
        }
    }

    fun saveTrace(trace: String, inputClass: String, isStatic: Boolean) {
        database.insertOrUpdate(SequenceEntity) {
            set(it.trace, trace)
            set(it.klass, inputClass)
            set(it.isStatic, isStatic)
            set(it.count, 1)
            onConflict {
                set(it.count, it.count.plus(1))
            }
        }
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

    fun getMethodsForClass(inputClass: String, isStatic: Boolean): HashSet<MethodData> {
        val methodsQuery = database.from(MethodEntity).select()
            .where(MethodEntity.klass.eq(inputClass).and(MethodEntity.isStatic.eq(isStatic)))
        val result = HashSet<MethodData>()
        methodsQuery.forEach {
            result.add(
                MethodData(
                    it[MethodEntity.klass]!!,
                    Json.decodeFromString(it[MethodEntity.args]!!),
                    it[MethodEntity.returnType]!!,
                    it[MethodEntity.isStatic]!!,
                    it[MethodEntity.klass]!!
                )
            )
        }
        return result
    }

    fun getTracesForClass(klass: String, isStatic: Boolean): HashSet<TraceHolder> {
        val tracesQuery = database.from(SequenceEntity).select(SequenceEntity.trace, SequenceEntity.count)
            .where(SequenceEntity.klass.eq(klass).and(SequenceEntity.isStatic.eq(isStatic)))
        val result = HashSet<TraceHolder>()
        tracesQuery.forEach { sequenceRaw ->
            result.add(
                TraceHolder(
                    Json.decodeFromString(sequenceRaw[SequenceEntity.trace]!!),
                    sequenceRaw[SequenceEntity.count]!!
                )
            )
        }
        return result
    }

}