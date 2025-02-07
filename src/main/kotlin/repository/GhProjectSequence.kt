package org.kechinvv.repository

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import config.Configurations
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request



class GhProjectsSequence(val lib: String, val client: OkHttpClient) : Sequence<RemoteRepository> {
    val linkGH = "https://api.github.com/search/code"

    private var page = 1
    private var lbound = 0
    private var rbound = 50

    private var total = 0

    val SLEEP: Long = 30_000
    val maxBound = 20000
    val maxPage = 10
    val maxRes = 1000


    private val inner = generateSequence {
        if (lbound > maxBound) null
        else {
            if (page > maxPage) {
                nextBounds()
                page = 1
            }
            val response = makeRequest()
            val json = JsonParser.parseString(response).asJsonObject
            val reps = getReps(json)
            reps
        }
    }.flatten()

    private fun nextBounds() {
        val delta = (rbound - lbound) * 2
        lbound = rbound
        rbound += delta
    }

    private fun makeRequest(): String {
        val queryUrlBuilder = linkGH.toHttpUrl().newBuilder()
            .addQueryParameter("q", "$lib in:file language:java size:$lbound..$rbound")
            .addQueryParameter("per_page", "100")
            .addQueryParameter("page", page.toString())
        val request = Request.Builder()
            .url(queryUrlBuilder.build())
            .addHeader("Authorization", "Token ${Configurations.ghToken}")
            .build()
        return client.newCall(request).execute().body.string() ?: ""
    }

    private fun getReps(json: JsonObject): List<RemoteRepository> {
        val reps = mutableListOf<RemoteRepository>()
        if (json.has("message"))
            if (json.get("message").toString() == "\"Bad credentials\"" ||
                json.get("message").toString() == "Bad credentials"
            ) throw Exception("Bad credentials")
            else {
                println(json.get("message"))
                Thread.sleep(SLEEP)
            }
        else if (json.get("total_count").asInt > maxRes && (rbound - lbound > 1)) rbound -= (rbound - lbound) / 2
        else {
            val items = json.getAsJsonArray("items")
            if (items.size() == 0) {
                nextBounds()
                page = 1
            } else {
                items.forEach { reps.add(RemoteGithub((it as JsonObject).get("repository") as JsonObject, client)) }
                total += reps.size
                page++
            }
        }
        return reps
    }

    override fun iterator(): Iterator<RemoteRepository> = inner.iterator()

}