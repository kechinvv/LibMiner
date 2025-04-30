package org.kechinvv.entities

import kotlinx.serialization.Serializable

@Serializable
data class EntryFilter(
    val methodTags: List<String>,
    val methodName: String?,
    val classTags: List<String>,
    val className: String?
)
