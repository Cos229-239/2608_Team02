package com.cos229239.team02.oto.data.resource

import com.cos229239.team02.oto.data.safety.NpsParkOption
import com.cos229239.team02.oto.data.safety.SafetyCategory
import com.cos229239.team02.oto.data.safety.SafetyLevel
import com.cos229239.team02.oto.data.safety.SafetyNotification
import com.cos229239.team02.oto.data.safety.getSafetyJson
import com.cos229239.team02.oto.data.safety.optionalText

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.time.Instant
//National Park Service API
class NpsAlertClient(
    private val http: OkHttpClient,
    private val apiKey: String
){
    suspend fun getAlerts(
        parkCode: String
    ): List<SafetyNotification> = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) {
            "NPS API key is missing"
        }

        val code = parkCode.trim().lowercase()

        require(code.matches(Regex("[a-z0-9]{4,10}"))) {
            "Select a valid NPS park code"
        }
        val alerts = mutableListOf<SafetyNotification>()
        var start = 0

        while (true){
            val url = "https://developer.nps.gov/api/v1/alerts"
                .toHttpUrl()
                .newBuilder()
                .addQueryParameter("parkCode", code)
                .addQueryParameter("limit", "50")
                .addQueryParameter("start", start.toString())
                .build()

            val request = Request.Builder()
                .url(url)
                .header("X-Api-Key", apiKey)
                .header("Accept", "application/json")
                .build()

            val root = http.getSafetyJson(request)
            val retrievedAt = Instant.now().toString()
            val data = root.getJSONArray("data")
            val total = root.getString("total").toInt()

            if (data.length() == 0 && start < total) {
                throw IOException("Incomplete NPS response")
            }
            for (index in 0 until data.length()) {
                val item = data.getJSONObject(index)


                val category =
                    item.optionalText("category") ?: "Information"

                alerts += SafetyNotification(
                    id = "nps:${item.getString("id")}",

                    title = item.optionalText("title") ?: "Park notice",

                    details = item.optionalText("description")
                        ?: "Check the park website for details. ",

                    instruct =
                        "Review the official park notice for instructions",

                    affectedArea =
                        "Selected park: ${code.uppercase()} • $category",
                    category = SafetyCategory.AREA,

                    level = when (category.lowercase()) {
                        "danger" ->
                            SafetyLevel.SEVERE

                        "caution", "park closure" ->
                            SafetyLevel.MODERATE

                        else ->
                            SafetyLevel.MINOR
                    },

                    sourceID = "National Park Service",

                    sourceUrl = item.optionalText("url")
                        ?: ("https://www.nps.gov/$code/" +
                                "planyourvisit/conditions.htm"),

                    retrievedTime = retrievedAt,

                    lastVerification = retrievedAt,

                    expires = null
                )
            }
            start += data.length()

            if (start >= total) {
                break
            }

            if (start >= 5000) {
                throw IOException("NPS pagination limit reached")
            }
        }

        alerts.distinctBy { it.id }
    }
    suspend fun getParkOptions(): List<NpsParkOption> =
        withContext(Dispatchers.IO) {
            require(apiKey.isNotBlank()) {
                "NPS API key is missing"
            }
            val parks = mutableListOf<NpsParkOption>()
            var start = 0

            while (true) {
                val url = "https://developer.nps.gov/api/v1/parks"
                    .toHttpUrl()
                    .newBuilder()
                    .addQueryParameter("limit", "50")
                    .addQueryParameter("start", start.toString())
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .header("X-Api-Key", apiKey)
                    .header("Accept", "application/json")
                    .build()

                val root = http.getSafetyJson(request)
                val data = root.getJSONArray("data")
                val total = root.getString("total").toInt()

                if (data.length() == 0 && start < total) {
                    throw IOException("Incomplete park directory.")
                }
                for (index in 0 until data.length()) {
                    val item = data.getJSONObject(index)

                    parks += NpsParkOption(
                        parkCode = item.getString("parkCode"),
                        fullName = item.getString("fullName"),
                        states = item.optionalText("states").orEmpty()
                    )
                }

                start += data.length()

                if (start >= total) {
                    break
                }

                if (start >= 5000) {
                    throw IOException("Park directory pagination limit reached.")
                }
            }
            parks
                .distinctBy { it.parkCode }
                .sortedBy { it.fullName }
        }
}

