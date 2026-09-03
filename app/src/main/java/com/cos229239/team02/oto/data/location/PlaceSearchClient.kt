package com.cos229239.team02.oto.data.location

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Searches OpenStreetMap's Nominatim service for real locations
 * entered by the user.
 */
class PlaceSearchClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Searches for matching U.S. locations.
     *
     * At least 3 characters are required before a request is sent.
     */
    suspend fun search(
        query: String
    ): List<PlaceSuggestion> {

        if (query.length < 3) {
            return emptyList()
        }

        val encodedQuery = URLEncoder.encode(
            query,
            "UTF-8"
        )

        val url =
            "https://nominatim.openstreetmap.org/search" +
                    "?q=$encodedQuery" +
                    "&format=json" +
                    "&addressdetails=1" +
                    "&countrycodes=us" +
                    "&limit=4"

        val request = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "OTO-App/1.0 (trip-planning)"
            )
            .get()
            .build()

        return try {

            withContext(Dispatchers.IO) {

                client.newCall(request)
                    .execute()
                    .use { response ->

                        if (!response.isSuccessful) {
                            return@withContext emptyList()
                        }

                        val payload =
                            response.body?.string().orEmpty()

                        val results =
                            json.decodeFromString<List<NominatimResult>>(
                                payload
                            )

                        results.map { result ->

                            PlaceSuggestion(
                                name = result.displayName,
                                latitude = result.latitude.toDoubleOrNull(),
                                longitude = result.longitude.toDoubleOrNull()
                            )
                        }
                    }
            }

        } catch (error: CancellationException) {

            // Coroutine cancellation should still work normally.
            throw error

        } catch (error: Exception) {

            // If the request fails, just return no suggestions
            // instead of crashing the screen.
            emptyList()
        }
    }
}

/**
 * Simplified location result used by Plan Trip.
 */
data class PlaceSuggestion(
    val name: String,
    val latitude: Double?,
    val longitude: Double?
)

/**
 * Raw location result returned by OpenStreetMap Nominatim.
 */
@Serializable
private data class NominatimResult(

    @SerialName("display_name")
    val displayName: String,

    @SerialName("lat")
    val latitude: String,

    @SerialName("lon")
    val longitude: String
)