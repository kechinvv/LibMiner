package org.kechinvv.storage

import org.ktorm.schema.*

object MethodEntity : Table<Nothing>("method") {
    val id = int("id").primaryKey()
    val name = varchar("method_name")
    val args = varchar("args")
    val returnType = varchar("return_type")
    val klass = varchar("class")
    val isStatic = boolean("is_static")
}