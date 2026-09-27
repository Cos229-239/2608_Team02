package com.cos229239.team02.oto.data.trip

/**
 * A saved trip created from Plan Trip.
 */
data class TripPlan(
    val startingPointName: String,
    val startingLatitude: Double,
    val startingLongitude: Double,

    val destinationName: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,

    val isRoundTrip: Boolean,

    val departureDateMillis: Long,
    val returnDateMillis: Long?,

    val notes: String,

    /*
     * Optional trusted contact for trip check-ins.
     *
     * A trip can still be saved without either value.
     */
    val trustedContactName: String? = null,
    val trustedContactPhone: String? = null
)