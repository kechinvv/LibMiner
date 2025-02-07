package org.kechinvv.repository

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


val SONATYPE_BODY_TEMPLATE = Any::class::class.java.getResource("/sonabody.json")!!.readText(Charsets.UTF_8)

class MvnProjectSequence(val lib: String, val client: OkHttpClient) : Sequence<RemoteRepository> {
    val linkSONATYPE = "https://central.sonatype.com/api/internal/browse/dependents"

    private var page = 0
    private var searchTerm = ""

    val SLEEP: Long = 30_000
    private val pageSize = 20
    private val maxPageCount = 500

    var foundPageCount = 0

    private val inner = generateSequence {
        if (endOfSearch()) null
        else {
            if (endOfPages()) {
                nextTerm()
                page = 0
            }
            val response = makeRequest()
            page++
            val json = JsonParser.parseString(response).asJsonObject
            val reps = getReps(json)
            reps
        }
    }.flatten()

    private fun endOfSearch(): Boolean =
        endOfPages() && (searchTerm == "" && !needFilterByTerm() || searchTerm == "zzz")

    private fun needFilterByTerm(): Boolean = foundPageCount == maxPageCount
    private fun endOfPages(): Boolean = page == foundPageCount


    private fun nextTerm() {
        TODO()
    }

    private fun makeRequest(): String {
        val queryUrlBuilder = linkSONATYPE.toHttpUrl().newBuilder()
        val body = String.format(SONATYPE_BODY_TEMPLATE, lib, page, pageSize, searchTerm)
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(queryUrlBuilder.build())
            .post(body)
            .build()
        return client.newCall(request).execute().body.string()
    }

    private fun getReps(json: JsonObject): List<RemoteRepository> {
        foundPageCount = json.getAsJsonPrimitive("pageCount").asInt
        return emptyList()
    }

    override fun iterator() = inner.iterator()
}