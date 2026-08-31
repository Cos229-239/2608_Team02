package com.cos229239.team02.oto.data.resource

import com.cos229239.team02.oto.data.location.OtoLocation

/**
 * A point of interest pulled from Overpass (OpenStreetMap)
 * that is useful in a crisis.
 *
 * [types] holds the user-facing categories this resource belongs
 * to so the UI can filter the list client-side (works offline).
 * [location] is the resource's nearest known coordinate, used for
 * distance/sorting and future map links.
 */
data class NearbyResource(
    val name: String,
    val types: List<ResourceType>,
    val distanceKm: Double,
    val location: OtoLocation
) {
    val primaryType: ResourceType
        get() = types.firstOrNull() ?: ResourceType.Other
}
