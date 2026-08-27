package com.cos229239.team02.oto.data.location

/**
 * This Defines how the rest of OTO asks for location data.
 *
 * The UI should depend on this interface instead of talking
 * directly to Android or a specific map/location providerr.
 */
interface LocationRepository {

    //Returns the most recently known location, if available.//
    suspend fun getLastKnownLocation(): OtoLocation?

    //Requests a fresh current location, if available.//
    suspend fun getCurrentLocation(): OtoLocation?
}