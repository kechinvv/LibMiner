package org.kechinvv.storage

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object MethodEntity : Table<Nothing>("method") {
    val id = int("id").primaryKey()
    val name = varchar("name")
    val klass = varchar("class")
}