package com.cos229239.team02.oto.data.resource

import com.cos229239.team02.oto.data.location.OtoLocation

/**
 * Defines how the UI asks for nearby crisis resources.
 *
 * Implementations should be cache-first so the list works offline.
 */
interface ResourceRepository {

    /**
     * Returns nearby resources for [location] using the repository's
     * default search radius. Falls back to the network when the cache is
     * stale. If the default radius returns nothing and the device is
     * online, implementations may automatically widen the search.
     *
     * @param forceRefresh when `true`, skip the cache and fetch from the
     *        network first, only falling back to cached data if the
     *        request fails.  The normal path is cache-first.
     * @return a [ResourceResult] describing whether data came from cache
     *         or network and the radius that was used.
     */
    suspend fun getNearby(
        location: OtoLocation,
        forceRefresh: Boolean = false
    ): ResourceResult

    /**
     * Returns nearby resources for [location] at an explicit search
     * radius (in meters). Useful for a manual "expand search" action.
     */
    suspend fun getNearby(
        location: OtoLocation,
        radiusMeters: Int,
        forceRefresh: Boolean = false
    ): ResourceResult
}

/**
 * Outcome of a [ResourceRepository.getNearby] call.
 *
 * [fromCache] tells the UI whether the shown list is from the offline
 * cache (so it can report freshness).
 * [radiusMeters] is the search radius that produced [resources], letting
 * the UI label whether a wider/expanded search was used.
 * [error] carries a user-facing message when the network path failed
 * (an empty list may still have cached data behind [error]).
 */
data class ResourceResult(
    val resources: List<NearbyResource>,
    val fromCache: Boolean,
    val radiusMeters: Int,
    val error: String? = null
)
