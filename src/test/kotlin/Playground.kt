import org.junit.jupiter.api.Test
import org.kechinvv.storage.RepositoryEntity
import org.kechinvv.storage.Storage
import org.ktorm.dsl.insert
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class Playground {
    @Test
    fun test() {
        Files.write(Paths.get("./testtes.log"), "Hello World".toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }
}