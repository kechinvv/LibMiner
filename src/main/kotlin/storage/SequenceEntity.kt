package org.kechinvv.storage

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.text
import org.ktorm.schema.varchar

object SequenceEntity : Table<Nothing>("sequence") {
    val id = int("id").primaryKey()
    val trace = text("trace")
    val klass = varchar("class")
    val extract_method = varchar("extract_method")
    val count = int("samples")
}