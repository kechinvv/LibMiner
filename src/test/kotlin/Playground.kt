import org.junit.jupiter.api.Test
import org.kechinvv.storage.RepositoryEntity
import org.kechinvv.storage.Storage
import org.ktorm.dsl.insert

class Playground {
    @Test
    fun test() {
        val db = Storage("testik.db")
        db.database.insert(RepositoryEntity) {
            set(it.repo_name, "aaaaa")
            set(it.repo_source, "bbbbb")
        }
    }
}