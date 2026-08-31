package com.cos229239.team02.oto.data.resource

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context.CONNECTIVITY_SERVICE
import com.cos229239.team02.oto.data.location.OtoLocation

/**
 * Cache-first [ResourceRepository] backed by the Overpass API and an
 * offline [ResourceCache].
 */
class OverpassResourceRepository(
    context: Context,
    private val cache: ResourceCache = ResourceCache(context),
    private val client: OverpassClient = OverpassClient(),
    private val defaultRadiusMeters: Int = DEFAULT_RADIUS_METERS,
    private val expandedRadiusMeters: Int = EXPANDED_RADIUS_METERS,
    private val cacheTtlMillis: Long = DEFAULT_CACHE_TTL_MILLIS
) : ResourceRepository {

    private val connectivityManager =
        context.applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    override suspend fun getNearby(
        location: OtoLocation,
        forceRefresh: Boolean
    ): ResourceResult =
        getNearby(location, defaultRadiusMeters, autoExpand = true, forceRefresh = forceRefresh)

    override suspend fun getNearby(
        location: OtoLocation,
        radiusMeters: Int,
        forceRefresh: Boolean
    ): ResourceResult =
        getNearby(location, radiusMeters, autoExpand = false, forceRefresh = forceRefresh)

    private suspend fun getNearby(
        location: OtoLocation,
        radiusMeters: Int,
        autoExpand: Boolean,
        forceRefresh: Boolean = false
    ): ResourceResult {
        if (!forceRefresh) {
            cache.readCached(location, radiusMeters, cacheTtlMillis)?.let { cached ->
                return ResourceResult(
                    resources = cached,
                    fromCache = true,
                    radiusMeters = radiusMeters
                )
            }
        }

        if (isOnline()) {
            return runCatching {
                client.fetchNearby(location, radiusMeters)
            }.fold(
                onSuccess = { fetched ->
                    if (fetched.isEmpty() && autoExpand) {
                        // No results at the default radius while online —
                        // automatically widen the search for better coverage.
                        return getNearby(location, expandedRadiusMeters, autoExpand = false, forceRefresh = forceRefresh)
                    }
                    cache.write(location, radiusMeters, fetched)
                    ResourceResult(
                        resources = fetched,
                        fromCache = false,
                        radiusMeters = radiusMeters
                    )
                },
                onFailure = { error ->
                    // Network path failed; fall back to stale cache if present.
                    cache.readAny(location, radiusMeters)?.let { stale ->
                        return ResourceResult(
                            resources = stale,
                            fromCache = true,
                            radiusMeters = radiusMeters,
                            error = "Offline — showing last known results."
                        )
                    }
                    ResourceResult(
                        resources = emptyList(),
                        fromCache = false,
                        radiusMeters = radiusMeters,
                        error = "Couldn't load resources: ${error.message}"
                    )
                }
            )
        }

        // Offline: show whatever we have, even if stale.
        return cache.readAny(location, radiusMeters)?.let { cached ->
            ResourceResult(
                resources = cached,
                fromCache = true,
                radiusMeters = radiusMeters,
                error = "Offline — showing last known results."
            )
        } ?: ResourceResult(
            resources = emptyList(),
            fromCache = true,
            radiusMeters = radiusMeters,
            error = "No connection and no saved results nearby."
        )
    }

    private fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {
        const val DEFAULT_RADIUS_METERS = 10_000
        const val EXPANDED_RADIUS_METERS = 25_000
        const val DEFAULT_CACHE_TTL_MILLIS = 24L * 60 * 60 * 1000
    }
}
