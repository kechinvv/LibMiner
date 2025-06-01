import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import org.kechinvv.analysis.JazzerRunner
import org.kechinvv.repository.MvnProjectSequence
import org.kechinvv.utils.JazzerDownloader
import java.nio.file.Paths

class Playground {


    @Test
    fun testMvnGet() {
        val seq = MvnProjectSequence("com.squareup.okhttp/okhttp@2.7.5", OkHttpClient())
        val reps = seq.take(20).toList()
        println(reps)
        reps[2].cloneTo(Paths.get("C:\\Users\\valer\\IdeaProjects\\LibMiner\\outputtest"))
    }

    @Test
    fun testJazzer() {
        JazzerRunner().run(
            listOf(Paths.get("C:\\Users\\valer\\IdeaProjects\\LibMiner\\temp-gradle-project\\build\\separateLibs")),
            "ca.ibodrov.concord.testcontainers.Concord::start"
        )
    }


}