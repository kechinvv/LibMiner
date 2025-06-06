package org.kechinvv.storage

import org.ktorm.schema.*

object RepositoryEntity : Table<Nothing>("repository") {
    val id = int("id").primaryKey()
    val name = varchar("repo_name")
    val namespace = varchar("namespace")
    val version = varchar("version")
    val author = varchar("author")
    val url = varchar("url")
    val source = varchar("source")
    val path = varchar("path")
    val date = datetime("date")
}

