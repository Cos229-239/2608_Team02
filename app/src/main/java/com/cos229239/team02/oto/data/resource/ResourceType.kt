package com.cos229239.team02.oto.data.resource

/**
 * User-facing categories for filtering nearby resources.
 *
 * Each category maps to a bucket of OpenStreetMap
 * amenity/emergency/tourism/etc. tags (see NearbyResourceMapper).
 */
enum class ResourceType(val label: String) {
    Medical("Medical"),
    Emergency("Fire & Emergency"),
    Water("Water"),
    Food("Food"),
    Shelter("Shelter"),
    Other("Other")
}
