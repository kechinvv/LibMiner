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
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.support.sqlite.SQLiteDialect
import org.ktorm.support.sqlite.insertOrUpdate
import java.io.FileNotFoundException
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.createDirectories

class Storage(dbName: Path, private val cashSize: Int = 10000) {

    private val database: Database

    private var simpleRepoCash = LinkedHashSet<String>()

    private fun updateCash(url: String) {
        simpleRepoCash.add(url)
        if (simpleRepoCash.size > cashSize) simpleRepoCash.removeFirst()
    }

    init {
        dbName.parent.createDirectories()
        database = Database.connect(
            "jdbc:sqlite:$dbName",
            user = "root",
            password = "root",
            dialect = SQLiteDialect()
        )
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
            onConflict(it.trace, it.klass, it.extract_method) {
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
            onConflict(it.url) {
                doNothing()
            }
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
            onConflict(it.url) {
                doNothing()
            }
        }
        updateCash(repo.url)
    }

    fun repoWasFound(repo: RemoteRepository): Boolean {
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


    fun getTracesForClass(klass: String, extractMethod: ExtractMethod? = null): Set<TraceHolder> {
        val conditions = ArrayList<ColumnDeclaring<Boolean>>()
        conditions.add(SequenceEntity.klass.eq(klass))
        if (extractMethod != null) conditions.add(SequenceEntity.extract_method.eq(extractMethod.name))

        val tracesQuery = database.from(SequenceEntity)
            .select(SequenceEntity.trace, SequenceEntity.count, SequenceEntity.extract_method)
            .where(conditions.reduce { a, b -> a and b })
        val result = HashSet<TraceHolder>()
        tracesQuery.forEach { sequenceRaw ->
            result.add(
                TraceHolder(
                    Json.decodeFromString(sequenceRaw[SequenceEntity.trace]!!),
                    ExtractMethod.valueOf(sequenceRaw[SequenceEntity.extract_method]!!),
                    sequenceRaw[SequenceEntity.count]!!
                )
            )
        }
        return result
    }

}