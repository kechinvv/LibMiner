package org.kechinvv.storage

import kotlinx.serialization.json.Json
import org.kechinvv.entities.MethodData
import org.kechinvv.holders.TraceHolder
import org.kechinvv.repository.RemoteRepository
import org.kechinvv.repository.RepositoryData
import org.kechinvv.utils.ExtractMethod
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

    private var simpleRepoCash = LinkedHashSet<RepositoryData>()

    private fun updateCash(repo: RepositoryData) {
        simpleRepoCash.add(repo)
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

    fun saveRepo(repo: RemoteRepository) {
        val source = repo.getSourceType()
        val data = repo.repositoryData
        database.insertOrUpdate(RepositoryEntity) {
            set(it.name, data.name)
            set(it.namespace, data.group)
            set(it.version, data.version)
            set(it.author, data.author)
            set(it.url, data.url)
            set(it.source, source.name)
            set(it.date, LocalDateTime.now())
            onConflict(it.url) {
                doNothing()
            }
        }
        updateCash(data)
    }


    fun repoWasFound(repo: RemoteRepository): Boolean {
        val data = repo.repositoryData
        if (simpleRepoCash.contains(repo.repositoryData)) return true
        database.from(RepositoryEntity).select().where(RepositoryEntity.url.eq(data.url))
            .forEach {
                val name = it[RepositoryEntity.name]!!
                val version = it[RepositoryEntity.version]
                val author = it[RepositoryEntity.author]
                val url = it[RepositoryEntity.url]!!
                val group = it[RepositoryEntity.namespace]
                updateCash(RepositoryData(name, url, group, version, author))
                return true
            }
        return false
    }

    fun repoWasFoundIgnoreVersion(repo: RemoteRepository): Boolean {
        val data = repo.repositoryData
        if (simpleRepoCash.firstOrNull { it.name == data.name && it.author == data.author && it.group == data.group } != null) return true
        val conditions = ArrayList<ColumnDeclaring<Boolean>>()
        conditions.add(RepositoryEntity.name.eq(data.name))
        if (data.author != null) conditions.add(RepositoryEntity.author.eq(data.author))
        else conditions.add(RepositoryEntity.author.isNull())
        if (data.group != null) conditions.add(RepositoryEntity.namespace.eq(data.group))
        else conditions.add(RepositoryEntity.namespace.isNull())

        database.from(RepositoryEntity).select()
            .where(conditions.reduce { a, b -> a and b })
            .forEach {
                val name = it[RepositoryEntity.name]!!
                val version = it[RepositoryEntity.version]
                val author = it[RepositoryEntity.author]
                val url = it[RepositoryEntity.url]!!
                val group = it[RepositoryEntity.namespace]
                updateCash(RepositoryData(name, url, group, version, author))
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