import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import org.kechinvv.config.Configuration
import org.kechinvv.entities.EntryFilter
import org.kechinvv.repository.MvnProjectSequence
import org.kechinvv.repository.MvnRemoteRepository
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class Playground {
    @Test
    fun test() {
        Files.write(Paths.get("./testtes.log"), "Hello World".toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }

    @Test
    fun testMvnGet() {
        val seq = MvnProjectSequence("com.squareup.okhttp/okhttp@2.7.5", OkHttpClient())
        val reps = seq.take(20).toList()
        println(reps)
        reps[2].cloneTo(Paths.get("C:\\Users\\valer\\IdeaProjects\\LibMiner\\outputtest"))
    }


}