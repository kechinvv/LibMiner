package org.kechinvv.storage

import org.ktorm.schema.*

object RepositoryEntity : Table<Nothing>("repository") {
    val id = int("id").primaryKey()
    val name = varchar("name")
    val source = varchar("source")
}

