package com.cos229239.team02.oto.data.resource

import android.content.Context
import com.cos229239.team02.oto.data.location.OtoLocation
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.floor

/**
 * Offline cache for nearby resources.
 *
 * Resources are stored per coarse "location cell" so that nearby
 * requests reuse the same cached blob. Reads are cache-first; callers
 * fall back to the network when the cache is stale or missing.
 */
class ResourceCache(
    private val context: Context
) {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val cacheDir: File
        get() = File(context.filesDir, CACHE_DIR_NAME).apply { mkdirs() }

    /**
     * Returns cached resources for the cell + radius the given location
     * falls in, or null when there's no fresh-enough cache (missing or
     * stale).
     *
     * The search radius is part of the cache key so a wider search never
     * reuses a narrower (possibly empty) result.
     */
    fun readCached(
        location: OtoLocation,
        radiusMeters: Int,
        maxAgeMillis: Long
    ): List<NearbyResource>? {
        val file = cacheFileFor(location, radiusMeters)
        if (!file.exists()) return null

        return runCatching {
            val blob = json.decodeFromString<CachedResourceBlob>(file.readText())
            if (blob.radiusMeters != radiusMeters) return@runCatching null
            val age = System.currentTimeMillis() - blob.fetchedAtMillis
            if (age > maxAgeMillis) return@runCatching null
            // An empty result is never a satisfying cache hit — treat it as
            // a miss so we re-fetch. This avoids serving a stale "nothing
            // found" blob (e.g. from a transient Overpass empty response).
            blob.toResources().takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    /**
     * Persists [resources] for the given location and search radius.
     * Callers may save even stale data when offline so it can be shown
     * later.
     */
    fun write(
        location: OtoLocation,
        radiusMeters: Int,
        resources: List<NearbyResource>
    ) {
        // Never persist an empty result — an empty response is likely a
        // transient Overpass hiccup and would otherwise poison the cache
        // for the whole TTL.
        if (resources.isEmpty()) return

        runCatching {
            val blob = CachedResourceBlob(
                cellKey = cellKeyFor(location, radiusMeters),
                radiusMeters = radiusMeters,
                fetchedAtMillis = System.currentTimeMillis(),
                resources = resources.map { it.toCached() }
            )
            cacheFileFor(location, radiusMeters).writeText(json.encodeToString(blob))
        }
    }

    /**
     * Reads whatever is cached for a location + radius regardless of age.
     * Used as a last-resort offline fallback.
     */
    fun readAny(
        location: OtoLocation,
        radiusMeters: Int
    ): List<NearbyResource>? {
        val file = cacheFileFor(location, radiusMeters)
        if (!file.exists()) return null
        return runCatching {
            json.decodeFromString<CachedResourceBlob>(file.readText()).toResources()
        }.getOrNull()
    }

    private fun cacheFileFor(location: OtoLocation, radiusMeters: Int): File {
        val cell = cellKeyFor(location, radiusMeters)
        return File(cacheDir, "$cell.json")
    }

    private fun cellKeyFor(location: OtoLocation, radiusMeters: Int): String {
        val lat = floor(location.latitude / CELL_SIZE_DEGREES).toInt()
        val lon = floor(location.longitude / CELL_SIZE_DEGREES).toInt()
        return "${lat}_${lon}_r${radiusMeters}"
    }

    private fun CachedResourceBlob.toResources(): List<NearbyResource> =
        resources.map {
            NearbyResource(
                name = it.name,
                types = it.types.mapNotNull { typeName ->
                    ResourceType.entries.firstOrNull { type -> type.name == typeName }
                },
                distanceKm = it.distanceKm,
                location = OtoLocation(
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            )
        }

    private fun NearbyResource.toCached(): CachedResource =
        CachedResource(
            name = name,
            types = types.map { it.name },
            distanceKm = distanceKm,
            latitude = location.latitude,
            longitude = location.longitude
        )

    private companion object {
        const val CACHE_DIR_NAME = "nearby_resources"
        // ~5km cells; large enough that nearby refreshes share one blob.
        const val CELL_SIZE_DEGREES = 0.05
    }
}
