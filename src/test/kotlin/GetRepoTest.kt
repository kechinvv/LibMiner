import okhttp3.OkHttpClient
import org.junit.jupiter.api.assertDoesNotThrow
import org.kechinvv.config.ProjectsConfiguration
import org.kechinvv.repository.GhProjectsSequence
import org.kechinvv.repository.MvnProjectSequence
import kotlin.test.Ignore
import kotlin.test.Test

class GetRepoTest {


    @Test
    @Ignore
    fun ghGetReposTest() {
        val configuration = ProjectsConfiguration()
        configuration.ghQuerySearch = "test"
        configuration.ghLanguageSearch = "java"
        configuration.ghToken = System.getenv("GH_TOKEN")
        val sequence = GhProjectsSequence(configuration, OkHttpClient())
        assertDoesNotThrow { sequence.elementAt(1001) }
    }

    @Test
    @Ignore
    fun getMavenCentralTest() {
        val configuration = ProjectsConfiguration()
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