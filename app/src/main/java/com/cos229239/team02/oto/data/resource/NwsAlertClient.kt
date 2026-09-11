package com.cos229239.team02.oto.data.resource

import com.cos229239.team02.oto.data.location.OtoLocation
import com.cos229239.team02.oto.data.safety.SafetyCategory
import com.cos229239.team02.oto.data.safety.SafetyLevel

import com.cos229239.team02.oto.data.safety.SafetyNotification
import com.cos229239.team02.oto.data.safety.getSafetyJson
import com.cos229239.team02.oto.data.safety.optionalText
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.time.Instant
//National Weather Service aPI Client
class NwsAlertClient(
    private val http: OkHttpClient,
    private val userAgent: String
) {

    suspend fun getAlerts(
        location: OtoLocation
    ): List<SafetyNotification> = withContext(Dispatchers.IO) {
        require(userAgent.isNotBlank()) {
            "NWS User-Agent is missing."
        }
        require(
            location.latitude.isFinite() &&
                    location.latitude in -90.0..90.0 &&
                    location.longitude.isFinite() &&
                    location.longitude in -180.0..180.0
        ) {
            "Invalid coordinates."
        }

        var nextUrl: HttpUrl? =
            "https://api.weather.gov/alerts/active"
                .toHttpUrl()
                .newBuilder()
                .addQueryParameter(
                    "point",
                    "${location.latitude},${location.longitude}"
                )
                .build()

        val visitedUrls = mutableSetOf<String>()
        val alerts = mutableListOf<SafetyNotification>()

        while (true) {
            val url = nextUrl ?: break

            require(
                url.scheme == "https" &&
                        url.host == "api.weather.gov"
            ) {
                "Unexcepted NWS pagination URL."
            }

            if (!visitedUrls.add(url.toString())) {
                throw IOException("Repeated NWS result page.")
            }

            if (visitedUrls.size > 100) {
                throw IOException("NWS pagination limit reached.")
            }
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept", "application/geo+json")
                .build()

            val root = http.getSafetyJson(request)
            val retrievedAt = Instant.now().toString()
            val features = root.getJSONArray("features")


            for (index in 0 until features.length()) {
                val feature = features.getJSONObject(index)
                val properties = feature.getJSONObject("properties")

                val id = properties.optionalText("id")
                    ?: feature.optionalText("id")
                    ?: throw IOException("Missing NWS alert ID.")

                val severity =
                    properties.optionalText("severity") ?: "Unknown"

                alerts += SafetyNotification(
                    id = "nws:$id",

                    title = properties.optionalText("headline")
                        ?: properties.optionalText("event")
                        ?: "Weather alert",

                    details = properties.optionalText("description")
                        ?: "No description provided.",

                    instruct = properties.optionalText("instruction")
                        ?: "Check the official source for guidance.",

                    affectedArea = properties.optionalText("areaDesc")
                        ?: "Selected location",
                   category = SafetyCategory.WEATHER,

                    level = when (severity.lowercase()) {
                        "extreme", "severe" ->
                            SafetyLevel.SEVERE

                        "moderate", "minor" ->
                            SafetyLevel.MODERATE

                        else -> SafetyLevel.MINOR
                    },

                    sourceID = "National Weather Service",

                    sourceUrl = properties.optionalText("web")
                        ?: ("https://www.weather.gov/" +
                                "?lat=${location.latitude}" +
                                "&lon=${location.longitude}"),

                    retrievedTime = retrievedAt,

                    // Records last successful fetch
                    lastVerification = retrievedAt,

                    expires = properties.optionalText("expires"),

                )
            }

            val next = root
                .optJSONObject("pagination")
                ?.optionalText("next")

            nextUrl = if (next == null) {
                null
            } else {
                url.resolve(next)
                    ?: throw IOException("Invalid NWS pagination URL.")
            }

        }
        alerts.distinctBy { it.id }
    }
    suspend fun getForecast(
        location: OtoLocation
    ): WeatherForecast = withContext(Dispatchers.IO) {
        require(userAgent.isNotBlank()) {
            "NWS User-Agent is missing."
        }

        require(
            location.latitude.isFinite() &&
                    location.latitude in -90.0..90.0 &&
                    location.longitude.isFinite() &&
                    location.longitude in -180.0..180.0
        ) {
            "Invalid coordinates."
        }

        val coordinates = String.format(
            Locale.US,
            "%.4f,%.4f",
            location.latitude,
            location.longitude
        )

        val pointRequest = Request.Builder()
            .url("https://api.weather.gov/points/$coordinates")
            .header("User-Agent", userAgent)
            .header("Accept", "application/geo+json")
            .build()

        val pointRoot = http.getSafetyJson(pointRequest)

        val forecastUrl = pointRoot
            .getJSONObject("properties")
            .optionalText("forecast")
            ?.toHttpUrl()
            ?: throw IOException("No forecast available for this location.")

        require(
            forecastUrl.scheme == "https" &&
                    forecastUrl.host == "api.weather.gov"
        ) {
            "Unexpected NWS forecast URL."
        }

        val forecastRequest = Request.Builder()
            .url(forecastUrl)
            .header("User-Agent", userAgent)
            .header("Accept", "application/geo+json")
            .build()

        val forecastRoot = http.getSafetyJson(forecastRequest)

        val periods = forecastRoot
            .getJSONObject("properties")
            .getJSONArray("periods")

        if (periods.length() == 0) {
            throw IOException("NWS returned no forecast periods.")
        }

        val period = periods.getJSONObject(0)
        val precipitation = period.optJSONObject("probabilityOfPrecipitation")

        WeatherForecast(
            periodName = period.getString("name"),
            temp = period.getInt("temperature"),
            tempUnit = period.getString("temperatureUnit"),
            shortForecast = period.optionalText("shortForecast")
                ?: "Description unavailable",
            detailedForecast = period.optionalText("detailedForecast")
                ?: "Details unavailable",
            windSpeed = period.optionalText("windSpeed") ?: "Unavailable",
            windDirection = period.optionalText("windDirection").orEmpty(),
            precipitationPercent = if (
                precipitation != null &&
                precipitation.has("value") &&
                !precipitation.isNull("value")
            ) {
                precipitation.getInt("value")
            } else {
                null
            },
            startTime = period.getString("startTime"),
            endTime = period.getString("endTime")
        )
    }
}



