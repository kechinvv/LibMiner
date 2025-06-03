import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import org.kechinvv.analysis.JazzerRunner
import org.kechinvv.config.Configuration
import org.kechinvv.repository.JarLocalRepository
import org.kechinvv.repository.MvnProjectSequence
import org.kechinvv.utils.JazzerDownloader
import org.kechinvv.utils.logger
import java.nio.file.Paths

class Playground {
    companion object {
        val LOG by logger()
    }

    @Test
    fun testMvnGet() {
        val seq = MvnProjectSequence("com.squareup.okhttp/okhttp@2.7.5", OkHttpClient())
        val reps = seq.take(20).toList()
        println(reps)
        val res = reps[2].cloneTo(Paths.get("C:\\Users\\valer\\IdeaProjects\\LibMiner\\outputtest")) as JarLocalRepository
        LOG.info(res.jarName)
    }

    @Test
    fun testJazzer() {
//        JazzerRunner(100, 100).run(
//            listOf(Paths.get("C:\\Users\\valer\\IdeaProjects\\LibMiner\\temp-gradle-project\\build\\separateLibs")),
//            "ca.ibodrov.concord.testcontainers.Concord::start"
//        )
    }


}