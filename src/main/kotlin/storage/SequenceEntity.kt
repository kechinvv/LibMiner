package org.kechinvv.storage

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import org.ktorm.schema.text
import org.ktorm.schema.boolean

object SequenceEntity : Table<Nothing>("sequence") {
    val id = int("id").primaryKey()
    val trace = text("trace")
    val klass = varchar("class")
    val isStatic = boolean("is_static")
    val count = int("samples")
}