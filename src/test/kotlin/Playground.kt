import org.junit.jupiter.api.Test
import org.kechinvv.repository.MvnRemoteRepository
import org.kechinvv.utils.logger
import org.kechinvv.workflow.TestOkhttpWorkflow
import java.nio.file.Paths

class Playground {
    companion object {
        val LOG by logger()
    }

    @Test
    fun testMvnGet() {
//        val seq = MvnProjectSequence("com.squareup.okhttp", "okhttp", "2.7.5", OkHttpClient())
//        val reps = seq.take(20).toList()
//        println(reps)
//        val res = reps[19].cloneTo(Paths.get("C:\\Users\\valer\\IdeaProjects\\LibMiner\\outputtest")) as JarLocalRepository
//        LOG.info(res.targetJar.toString())
 //       MvnRemoteRepository("okhttp", "com.squareup.okhttp",  "2.7.5").cloneTo(Paths.get("C:\\Users\\valer\\IdeaProjects\\LibMiner\\outputtest"))
    }

    @Test
    fun testJazzer() {
//        JazzerRunner(100, 100).run(
//            listOf(Paths.get("C:\\Users\\valer\\IdeaProjects\\LibMiner\\temp-gradle-project\\build\\separateLibs")),
//            "ca.ibodrov.concord.testcontainers.Concord::start"
//        )
 //       TestOkhttpWorkflow().getPrjs()
       // TestOkhttpWorkflow().traceCollectSimple()
    }


}