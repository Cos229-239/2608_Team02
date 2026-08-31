package com.cos229239.team02.oto.data.resource

import kotlinx.serialization.Serializable

/**
 * A bounded Overpass "out center tags" response.
 */
@Serializable
internal data class OverpassResponse(
    val elements: List<OverpassElement> = emptyList()
)

/**
 * An Overpass element. `lat`/`lon` are present for nodes; `center`
 * is present for ways/relations when `out center` is used.
 */
@Serializable
internal data class OverpassElement(
    val type: String = "",
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenter? = null,
    val tags: Map<String, String> = emptyMap()
)

@Serializable
internal data class OverpassCenter(
    val lat: Double? = null,
    val lon: Double? = null
)

/**
 * Shape persisted to the offline cache file.
 */
@Serializable
internal data class CachedResourceBlob(
    val cellKey: String,
    val radiusMeters: Int,
    val fetchedAtMillis: Long,
    val resources: List<CachedResource>
)

@Serializable
internal data class CachedResource(
    val name: String,
    val types: List<String>,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double
)
