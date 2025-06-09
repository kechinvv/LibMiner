package org.kechinvv.storage

import kotlinx.serialization.json.Json
import org.kechinvv.entities.MethodData
import org.kechinvv.holders.TraceHolder
import org.kechinvv.repository.GhRemoteRepository
import org.kechinvv.repository.MvnRemoteRepository
import org.kechinvv.repository.RemoteRepository
import org.kechinvv.utils.ExtractMethod
import org.kechinvv.utils.PrjSource
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.support.sqlite.SQLiteDialect
import org.ktorm.support.sqlite.insertOrUpdate
import java.io.FileNotFoundException
import java.time.LocalDateTime

class Storage(dbName: String, private val cashSize: Int) {

    var database = Database.connect(
        "jdbc:sqlite:$dbName",
        user = "root",
        password = "root",
        dialect = SQLiteDialect()
    )

    private var simpleRepoCash = LinkedHashSet<String>()

    private fun updateCash(url: String) {
        simpleRepoCash.add(url)
        if (simpleRepoCash.size > cashSize) simpleRepoCash.removeFirst()
    }

    init {
        database.useConnection { conn ->
            conn.createStatement().use { statement ->
                val script = javaClass.classLoader.getResource("create_tables.sql")?.readText()
                    ?: throw FileNotFoundException("could not find init script")
                statement.executeUpdate(script)
            }
        }
    }


    fun saveTrace(trace: List<MethodData>, inputClass: String, extractMethod: ExtractMethod) {
        database.insertOrUpdate(SequenceEntity) {
            set(it.trace, Json.encodeToString(trace))
            set(it.klass, inputClass)
            set(it.count, 1)
            set(it.extract_method, extractMethod.name)
            onConflict {
                set(it.count, it.count.plus(1))
            }
        }
    }

    fun saveGhRepo(repo: GhRemoteRepository) {
        database.insertOrUpdate(RepositoryEntity) {
            set(it.name, repo.name)
            set(it.author, repo.author)
            set(it.url, repo.url)
            set(it.source, PrjSource.GITHUB.name)
            set(it.date, LocalDateTime.now())
        }
        updateCash(repo.url)
    }


    fun saveMvnRepo(repo: MvnRemoteRepository) {
        database.insertOrUpdate(RepositoryEntity) {
            set(it.name, repo.name)
            set(it.namespace, repo.group)
            set(it.version, repo.version)
            set(it.url, repo.url)
            set(it.source, PrjSource.MAVEN_CENTRAL.name)
            set(it.date, LocalDateTime.now())
        }
        updateCash(repo.url)
    }

    fun repoAlreadyAnalyzed(repo: RemoteRepository): Boolean {
        if (simpleRepoCash.contains(repo.url)) return true
        database.from(RepositoryEntity).select(RepositoryEntity.url).where(RepositoryEntity.url.eq(repo.url))
            .forEach {
                updateCash(it[RepositoryEntity.url]!!)
                return true
            }
        return false
    }


    fun getClasses(): HashSet<String> {
        val classesQuery = database.from(SequenceEntity).selectDistinct(SequenceEntity.klass)
        val res = HashSet<String>()
        classesQuery.forEach { res.add(it[SequenceEntity.klass]!!) }
        return res
    }


    fun getTracesForClass(klass: String): Set<TraceHolder> {
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