import okhttp3.OkHttpClient
import org.junit.jupiter.api.AfterEach
import org.kechinvv.config.Configuration
import org.kechinvv.entities.MethodData
import org.kechinvv.holders.TraceHolder
import org.kechinvv.repository.GhRemoteRepository
import org.kechinvv.repository.MvnRemoteRepository
import org.kechinvv.repository.RepositoryData
import org.kechinvv.storage.Storage
import org.kechinvv.utils.ExtractMethod
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.test.*

class StorageTest {
    val targetDb = Paths.get("testdb/test.db")

    @OptIn(ExperimentalPathApi::class)
    @AfterEach
    fun cleanup() {
        System.gc()
        targetDb.deleteRecursively()
    }

    private val trace1 =
        listOf(
            MethodData("a", listOf(), "void", false, "test"),
            MethodData("b", listOf(), "void", false, "test"),
            MethodData("c", listOf(), "void", false, "test")
        )


    private val trace2 =
        listOf(
            MethodData("a", listOf(), "void", false, "test2"),
            MethodData("b", listOf(), "void", false, "test2"),
            MethodData("c", listOf(), "void", false, "test2")
        )

    private val trace3 =
        listOf(
            MethodData("a", listOf(), "void", false, "test"),
            MethodData("b", listOf(), "void", false, "test"),
            MethodData("d", listOf(), "void", false, "test")
        )


    @Test
    fun saveSameTraceTest() {
        val traceKlass = trace1.first().klass
        val storage = Storage(targetDb)
        assert(targetDb.exists())
        for (i in 1..4) {
            storage.saveTrace(trace1, traceKlass, ExtractMethod.STATIC)
            val traceFromStorage = storage.getTracesForClass(traceKlass)
            assert(traceFromStorage == setOf(TraceHolder(trace1, ExtractMethod.STATIC, i)))
        }
    }

    @Test
    fun saveDifferentTraceTest() {
        val storage = Storage(targetDb)
        assert(targetDb.exists())
        val traceHolders =
            setOf(trace1, trace2, trace3).distinct()
        val rowsForClass = traceHolders.map { it.first().klass }.toSet().associateWith { 0 }.toMutableMap()
        traceHolders.forEach {
            val klass = it.first().klass
            storage.saveTrace(it, klass, ExtractMethod.STATIC)
            storage.saveTrace(it, klass, ExtractMethod.DYNAMIC)
            rowsForClass[klass] = rowsForClass[klass]!! + 2
        }
        rowsForClass.forEach { (klass, count) ->
            val traceFromStorage = storage.getTracesForClass(klass)
            assertEquals(count, traceFromStorage.size)
            val traceFromStorageDynamic = storage.getTracesForClass(klass, ExtractMethod.DYNAMIC)
            assertEquals(count / 2, traceFromStorageDynamic.size)
            val traceFromStorageStatic = storage.getTracesForClass(klass, ExtractMethod.STATIC)
            assertEquals(count / 2, traceFromStorageStatic.size)
        }
    }

    private val configuration = Configuration()
    val repos = setOf(
        GhRemoteRepository(
            RepositoryData(name = "name", url = "stub1.url", author = "author"),
            OkHttpClient(),
            configuration
        ),
        GhRemoteRepository(
            RepositoryData(name = "name", url = "stub2.url", author = "author"),
            OkHttpClient(),
            configuration
        ),
        GhRemoteRepository(
            RepositoryData(name = "name", url = "stub3.url", author = "author"),
            OkHttpClient(),
            configuration
        ),
        MvnRemoteRepository("name", "group", "version1"),
        MvnRemoteRepository("name", "group", "version2"),
        MvnRemoteRepository("name", "group", "version3")
    )


    @Test
    fun saveRepository() {
        val storage = Storage(targetDb, 2)
        assert(targetDb.exists())
        for (repo in repos) {
            assertFalse { storage.repoWasFound(repo) }
            storage.saveRepo(repo)
        }
        for (repo in repos) {
            assertTrue { storage.repoWasFound(repo) }
        }
    }


}