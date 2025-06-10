package org.kechinvv.repository

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.kechinvv.config.Configuration
import org.kechinvv.utils.logger


class GhProjectsSequence(val client: OkHttpClient, val configuration: Configuration) :
    Sequence<RemoteRepository> {
    companion object {
        val LOG by logger()
    }

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
            LOG.debug(response)
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
        val langFilter =
            if (configuration.ghLanguageSearch != null) "language:${configuration.ghLanguageSearch}" else ""
        val filenameFilter = if (configuration.ghFileName != null) "filename:${configuration.ghFileName}" else ""
        val queryUrlBuilder = linkGH.toHttpUrl().newBuilder()
            .addQueryParameter(
                "q",
                "${configuration.ghQuerySearch} in:file $langFilter $filenameFilter size:$lbound..$rbound"
            )
            .addQueryParameter("per_page", "100")
            .addQueryParameter("page", page.toString())
        val request = Request.Builder()
            .url(queryUrlBuilder.build())
            .addHeader("Authorization", "Token ${configuration.ghToken}")
            .build()
        return client.newCall(request).execute().body.string()
    }

    private fun getReps(json: JsonObject): List<RemoteRepository> {
        val reps = mutableListOf<RemoteRepository>()
        if (json.has("message"))
            if (json.get("message").toString() == "\"Bad credentials\"" ||
                json.get("message").toString() == "Bad credentials"
            ) throw Exception("Bad credentials")
            else {
                LOG.info("GH return msg: ${json.get("message")}")
                Thread.sleep(SLEEP)
            }
        else if (json.get("total_count").asInt > maxRes && (rbound - lbound > 1)) rbound -= (rbound - lbound) / 2
        else {
            val items = json.getAsJsonArray("items")
            if (items.size() == 0) {
                nextBounds()
                page = 1
            } else {
                items.forEach {
                    reps.add(
                        GhRemoteRepository(
                            (it as JsonObject).get("repository") as JsonObject,
                            client,
                            configuration
                        )
                    )
                }
                total += reps.size
                page++
            }
        }
        return reps
    }

    override fun iterator(): Iterator<RemoteRepository> = inner.iterator()

}