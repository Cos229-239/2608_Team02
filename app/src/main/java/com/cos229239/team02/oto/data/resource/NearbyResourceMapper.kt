package com.cos229239.team02.oto.data.resource

import com.cos229239.team02.oto.data.location.OtoLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Maps raw Overpass elements to [NearbyResource] and computes the
 * straight-line (Haversine) distance from the user's location.
 */
internal object NearbyResourceMapper {

    fun map(
        elements: List<OverpassElement>,
        origin: OtoLocation
    ): List<NearbyResource> =
        elements
            .mapNotNull { element ->
                val lat = element.lat ?: element.center?.lat
                val lon = element.lon ?: element.center?.lon
                if (lat == null || lon == null) return@mapNotNull null

                val types = typeFor(element.tags)
                if (types.isEmpty()) return@mapNotNull null

                val location = OtoLocation(latitude = lat, longitude = lon)
                val name = element.tags["name"]
                    ?.takeIf { it.isNotBlank() }
                    ?: fallbackName(types)

                NearbyResource(
                    name = name,
                    types = types,
                    distanceKm = distanceKm(origin, location),
                    location = location
                )
            }
            .sortedBy { it.distanceKm }

    /**
     * Maps OSM tags to one or more [ResourceType] buckets.
     * Returns empty if the element isn't a useful crisis resource.
     */
    private fun typeFor(tags: Map<String, String>): List<ResourceType> {
        val types = LinkedHashSet<ResourceType>()

        val amenity = tags["amenity"]
        val emergency = tags["emergency"]
        val tourism = tags["tourism"]
        val shop = tags["shop"]

        when (amenity) {
            "hospital", "clinic", "doctors", "dentist", "pharmacy", "veterinary", "nursing_home" -> {
                types += ResourceType.Medical
            }
            "fire_station", "police" -> {
                types += ResourceType.Emergency
            }
            "shelter", "social_facility", "place_of_worship", "community_centre" -> {
                types += ResourceType.Shelter
            }
            "drinking_water", "fountain" -> {
                types += ResourceType.Water
            }
            "restaurant", "fast_food", "cafe", "pub", "mall", "marketplace" -> {
                types += ResourceType.Food
            }
        }

        when (emergency) {
            "hospital", "ambulance_station", "fire_hydrant" -> types += ResourceType.Emergency
            "drinking_water" -> types += ResourceType.Water
        }

        when (tourism) {
            "camp_site", "information" -> if (types.isEmpty()) types += ResourceType.Shelter
        }

        when (amenity) {
            "school", "library" -> if (types.isEmpty()) types += ResourceType.Shelter
        }

        when (shop) {
            "convenience", "supermarket" -> types += ResourceType.Food
        }

        return types.toList()
    }

    private fun fallbackName(types: List<ResourceType>): String {
        val primary = types.firstOrNull()?.label ?: "Resource"
        return "Unnamed $primary"
    }

    private fun distanceKm(from: OtoLocation, to: OtoLocation): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val a =
            sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(from.latitude)) *
                cos(Math.toRadians(to.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}
