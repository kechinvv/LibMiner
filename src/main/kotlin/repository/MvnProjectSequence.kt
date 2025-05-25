package org.kechinvv.repository

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody




class MvnProjectSequence(val lib: String, val client: OkHttpClient) : Sequence<RemoteRepository> {
    private val linkSonatype = "https://central.sonatype.com/api/internal/browse/dependents"
    private val sonatypeBodyTemplate = this::class.java.getResource("/sonabody.json")!!.readText(Charsets.UTF_8)

    private var page = 0
    private var searchTerm = Term()

    private val pageSize = 20
    private val maxPageCount = 500

    var foundPageCount = -1

    private val inner = generateSequence {
        if (endOfSearch()) null
        else {
            if (endOfPages()) {
                nextTerm()
                page = 0
            }
            val response = makeRequest()
            page++
            val reps = getReps(response)
            reps
        }
    }.flatten()

    private fun endOfSearch(): Boolean =
        endOfPages() && (searchTerm.isEmpty() && !needFilterByTerm() || searchTerm.isLast())

    private fun needFilterByTerm(): Boolean = foundPageCount == maxPageCount
    private fun endOfPages(): Boolean = page == foundPageCount


    private fun nextTerm() {
        searchTerm.next()
        var pages = maxPageCount
        while (pages == maxPageCount || pages == 0) {
            val response = makeRequest()
            pages = response.getAsJsonPrimitive("pageCount").asInt
            if (pages == maxPageCount) searchTerm.expandTerm()
            else if (pages == 0) searchTerm.next()
        }
    }

    private fun makeRequest(): JsonObject {
        val queryUrlBuilder = linkSonatype.toHttpUrl().newBuilder()
        val body = String.format(sonatypeBodyTemplate, lib, page, pageSize, searchTerm)
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(queryUrlBuilder.build())
            .post(body)
            .build()
        val response = client.newCall(request).execute().body.string()
        return JsonParser.parseString(response).asJsonObject
    }

    private fun getReps(json: JsonObject): List<RemoteRepository> {
        foundPageCount = json.getAsJsonPrimitive("pageCount").asInt
        val jsonReps = json.getAsJsonArray("components")
        val res = mutableListOf<RemoteRepository>()
        jsonReps.forEach {
            val item = it.asJsonObject
            val namespace = item.get("sourceNamespace").asString
            val name = item.get("sourceName").asString
            val version = item.get("sourceVersion").asString
            res.add(MvnRemoteRepository(namespace, name, version))
        }
        return res
    }

    override fun iterator() = inner.iterator()
}

private class Term {
    val term: ArrayList<Char> = arrayListOf()

    fun next() {
        for (i in term.size - 1 downTo 0) {
            if (term[i] != 'z') {
                term[i] = term[i] + 1
                break
            } else {
                term.dropLast(1)
            }
        }

    }

    fun expandTerm() {
        term.add('a')
    }

    fun isEmpty(): Boolean = term.isEmpty()
    fun isLast(): Boolean = term.fold(true) { acc, el -> acc && el == 'z' }

    override fun toString(): String {
        return term.joinToString(separator = "")
    }
}