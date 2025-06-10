import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.kechinvv.config.FsmConfiguration
import org.kechinvv.entities.MethodData
import org.kechinvv.holders.TraceHolder
import org.kechinvv.inference.Automaton
import org.kechinvv.inference.FSMInference
import org.kechinvv.storage.Storage
import org.kechinvv.utils.ExtractMethod
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.readText
import kotlin.test.Test

class FsmTest {
    private var configuration = FsmConfiguration()

    @OptIn(ExperimentalPathApi::class)
    @AfterEach
    fun cleanup() {
        System.gc()
        configuration.mintFilesPath.deleteRecursively()
        configuration = FsmConfiguration()
    }

    private val traceHolder1 = TraceHolder(
        listOf(
            MethodData("a", listOf(), "void", false, "test"),
            MethodData("b", listOf(), "void", false, "test"),
            MethodData("c", listOf(), "void", false, "test")
        ), ExtractMethod.STATIC, 1
    )

    private val traceHolder2 = TraceHolder(
        listOf(
            MethodData("a", listOf(), "void", false, "test"),
            MethodData("b", listOf(), "void", false, "test"),
            MethodData("d", listOf(), "void", false, "test")
        ), ExtractMethod.STATIC, 1
    )

    private val traceHolder3 = TraceHolder(
        listOf(
            MethodData("a", listOf(), "void", false, "test"),
            MethodData("b", listOf(), "void", false, "test"),
            MethodData("e", listOf(), "void", false, "test")
        ), ExtractMethod.STATIC, 1
    )

    @Test
    fun inferenceTest() {
        val storage = mockk<Storage>()
        val traces = setOf(traceHolder1)
        every { storage.getTracesForClass(any()) } returns traces
        val inference = FSMInference(configuration, storage)
        val result = inference.inferenceByClass("any").json?.readText()
        val reference = this::class.java.getResource("simpleHandledRef.json")!!.readText()
        val refFsm = Json.decodeFromString<Automaton>(reference)
        val resultFsm = result?.let { Json.decodeFromString<Automaton>(it) }
        assert(refFsm == resultFsm)
    }

    @Test
    fun inferenceUnionEndTest() {
        val storage = mockk<Storage>()
        val traces = setOf(traceHolder1, traceHolder2, traceHolder3)
        every { storage.getTracesForClass(any()) } returns traces
        val inference = FSMInference(configuration, storage)
        val result = inference.inferenceByClass("any").json?.readText()
        val reference = this::class.java.getResource("unionEndRef.json")!!.readText()
        val refFsm = Json.decodeFromString<Automaton>(reference)
        val resultFsm = result?.let { Json.decodeFromString<Automaton>(it) }
        assert(refFsm == resultFsm)
    }

    @Test
    fun inferenceNotUnionEndTest() {
        val storage = mockk<Storage>()
        val traces = setOf(traceHolder1, traceHolder2, traceHolder3)
        configuration.unionEnd = false
        every { storage.getTracesForClass(any()) } returns traces
        val inference = FSMInference(configuration, storage)
        val result = inference.inferenceByClass("any").json?.readText()
        val reference = this::class.java.getResource("NotUnionEndRef.json")!!.readText()
        val refFsm = Json.decodeFromString<Automaton>(reference)
        val resultFsm = result?.let { Json.decodeFromString<Automaton>(it) }
        assert(refFsm == resultFsm)
    }
}