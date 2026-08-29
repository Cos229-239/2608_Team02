package com.cos229239.team02.oto.data.location


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import android.annotation.SuppressLint


/* Android implementation of LocationRepository. Uses Google's Fused Location Provider to obtain device location.
Permission requests are handled by the UI layer.
If location permission has not been granted, this repository safely returns null.*/


class AndroidLocationRepository(
    private val context: Context
) : LocationRepository {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): OtoLocation? {

        if (!hasLocationPermission()) {
            return null
        }

        return awaitLocation(
            fusedLocationClient.lastLocation
        )?.toOtoLocation()
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): OtoLocation? {

        if (!hasLocationPermission()) {
            return null
        }

        val task = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        )

        return awaitLocation(task)?.toOtoLocation()
    }

    private fun hasLocationPermission(): Boolean {

        val fineLocationGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted || coarseLocationGranted
    }

    private suspend fun awaitLocation(
        task: Task<Location>
    ): Location? =
        suspendCancellableCoroutine { continuation ->

            task.addOnSuccessListener { location ->
                if (continuation.isActive) {
                    continuation.resume(location)
                }
            }

            task.addOnFailureListener {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }

            task.addOnCanceledListener {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }

    private fun Location.toOtoLocation(): OtoLocation =
        OtoLocation(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracy,
            altitudeMeters = if (hasAltitude()) altitude else null,
            timestampMillis = time
        )
}