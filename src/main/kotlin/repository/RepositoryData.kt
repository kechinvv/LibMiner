package org.kechinvv.repository

data class RepositoryData(
    val name: String,
    val url: String,
    val group: String? = null,
    val version: String? = null,
    val author: String? = null,
)
