import okhttp3.OkHttpClient
import org.junit.jupiter.api.assertDoesNotThrow
import org.kechinvv.config.Configuration
import org.kechinvv.repository.GhProjectsSequence
import org.kechinvv.repository.MvnProjectSequence
import org.kechinvv.utils.logger
import kotlin.test.Ignore
import kotlin.test.Test

class GetRepoTest {

    companion object {
        val LOG by logger()
    }
    @Test
    @Ignore
    fun ghGetReposTest() {
        val configuration = Configuration()
        configuration.ghQuerySearch = "test"
        configuration.ghLanguageSearch = "java"
        configuration.ghToken = System.getenv("GH_TOKEN")
        val sequence = GhProjectsSequence(OkHttpClient(), configuration)
        assertDoesNotThrow { sequence.elementAt(1001) }
    }

    @Test
    @Ignore
    fun getMavenCentralTest() {
        val configuration = Configuration()
        configuration.targetLibGroup = "com.squareup.okhttp"
        configuration.targetLibName = "okhttp"
        configuration.targetLibVersion = "2.7.5"
        val sequence = MvnProjectSequence(
            configuration.targetLibGroup,
            configuration.targetLibName,
            configuration.targetLibVersion,
            OkHttpClient()
        )
        assertDoesNotThrow { sequence.elementAt(10002) }
    }

}