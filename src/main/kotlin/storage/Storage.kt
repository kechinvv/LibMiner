package org.kechinvv.storage

import kotlinx.serialization.json.Json
import org.kechinvv.holders.TraceHolder
import org.kechinvv.utils.ExtractMethod
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


    fun saveTrace(trace: String, inputClass: String, extractMethod: ExtractMethod) {
        database.insertOrUpdate(SequenceEntity) {
            set(it.trace, trace)
            set(it.klass, inputClass)
            set(it.count, 1)
            set(it.extract_method, extractMethod.method)
            onConflict {
                set(it.count, it.count.plus(1))
            }
        }
    }


    fun getClasses(): HashSet<String> {
        val classesQuery = database.from(SequenceEntity).selectDistinct(SequenceEntity.klass)
        val res = HashSet<String>()
        classesQuery.forEach { res.add(it[SequenceEntity.klass]!!)}
        return res
    }


    fun getTracesForClass(klass: String): HashSet<TraceHolder> {
        val tracesQuery = database.from(SequenceEntity).select(SequenceEntity.trace, SequenceEntity.count)
            .where(SequenceEntity.klass.eq(klass))
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