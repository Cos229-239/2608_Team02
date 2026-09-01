package com.cos229239.team02.oto.data.resource

import com.cos229239.team02.oto.data.location.OtoLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Thin client for the Overpass (OpenStreetMap) API.
 *
 * Sends a compact query that returns structured points of interest
 * (POIs) near a location, using `out center` so both nodes and
 * ways come back with coordinates.
 */
class OverpassClient(
    private val endpoints: List<String> = DEFAULT_ENDPOINTS
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Fetches nearby crisis resources for [location].
     *
     * Tries each configured Overpass endpoint in order so an
     * overloaded or rate-limited public instance doesn't fail the whole
     * call.
     *
     * @throws Exception if every endpoint fails or responses can't be parsed.
     */
    suspend fun fetchNearby(
        location: OtoLocation,
        radiusMeters: Int
    ): List<NearbyResource> {
        val query = buildQuery(location, radiusMeters)

        var lastError: Throwable? = null
        for (endpoint in endpoints) {
            try {
                return fetch(endpoint, location, query)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                lastError = t
            }
        }
        throw lastError ?: IllegalStateException("No Overpass endpoints configured")
    }

    private suspend fun fetch(
        endpoint: String,
        location: OtoLocation,
        query: String
    ): List<NearbyResource> {
        // Overpass' POST endpoint accepts the query URL-encoded as a
        // `data` form field. This is the canonical format (also used by the
        // reference OTO implementation) and is the most reliable across
        // device networks.
        val formBody = "data=${URLEncoder.encode(query, "UTF-8")}"

        val request = Request.Builder()
            .url(endpoint)
            .header("User-Agent", "OTO-App/1.0 (crisis-mode)")
            .post(formBody.toRequestBody(QUERY_MEDIA_TYPE))
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Overpass returned HTTP ${response.code}")
                }
                val payload = response.body?.string().orEmpty()
                val parsed = json.decodeFromString<OverpassResponse>(payload)
                NearbyResourceMapper.map(parsed.elements, location)
            }
        }
    }

    private fun buildQuery(location: OtoLocation, radiusMeters: Int): String {
        val lat = String.format(Locale.US, "%.6f", location.latitude)
        val lon = String.format(Locale.US, "%.6f", location.longitude)
        val r = radiusMeters

        return """
            [out:json][timeout:25];
            (
              node(around:$r,$lat,$lon)["amenity"~"hospital|clinic|doctors|dentist|pharmacy|veterinary|nursing_home|fire_station|police|shelter|social_facility|place_of_worship|community_centre|drinking_water|fountain|restaurant|fast_food|cafe|pub|mall|marketplace|school|library"];
              way(around:$r,$lat,$lon)["amenity"~"hospital|clinic|doctors|dentist|pharmacy|veterinary|nursing_home|fire_station|police|shelter|social_facility|place_of_worship|community_centre|restaurant|fast_food|cafe|pub|mall|marketplace|school|library"];
              node(around:$r,$lat,$lon)["emergency"~"hospital|ambulance_station|drinking_water|fire_hydrant"];
              way(around:$r,$lat,$lon)["emergency"~"hospital|ambulance_station"];
              node(around:$r,$lat,$lon)["tourism"~"camp_site|information"];
              node(around:$r,$lat,$lon)["shop"~"supermarket|convenience"];
            );
            out center tags;
        """.trimIndent()
    }

    private companion object {
        val QUERY_MEDIA_TYPE = "application/x-www-form-urlencoded".toMediaType()

        val DEFAULT_ENDPOINTS = listOf(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.openstreetmap.fr/api/interpreter",
            "https://overpass.osm.ch/api/interpreter"
        )
    }
}
