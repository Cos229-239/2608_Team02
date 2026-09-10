package com.cos229239.team02.oto.data.route

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.cos229239.team02.oto.data.location.OtoLocation
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Streams ongoing GPS fixes while a route is being tracked.
 *
 * Uses Google's Fused Location Provider so the calls continue
 * to arrive while this screen stays in the foreground.
 * Permission requests are handled by the UI layer; without
 * permission this tracker safely does nothing.
 */
class RouteTracker(
    private val context: Context
) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locations =
        MutableSharedFlow<OtoLocation>(
            extraBufferCapacity = 4
        )

    /** Live GPS fixes, collected while tracking is active. */
    val locations: SharedFlow<OtoLocation> =
        _locations.asSharedFlow()

    private val locationCallback =
        object : LocationCallback() {

            override fun onLocationResult(
                result: LocationResult
            ) {
                result.lastLocation?.let { location ->
                    _locations.tryEmit(
                        location.toOtoLocation()
                    )
                }
            }
        }

    private var isTracking = false

    val isActive: Boolean
        get() = isTracking

    /**
     * Starts receiving GPS fixes at roughly the given interval.
     */
    @SuppressLint("MissingPermission")
    fun start(
        intervalMillis: Long = DEFAULT_INTERVAL_MILLIS
    ) {

        if (isTracking || !hasLocationPermission()) {
            return
        }

        val request =
            LocationRequest.Builder(
                intervalMillis
            )
                .setMinUpdateIntervalMillis(
                    MIN_INTERVAL_MILLIS
                )
                .setPriority(
                    Priority.PRIORITY_HIGH_ACCURACY
                )
                .build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )

        isTracking = true
    }

    /**
     * Stops receiving GPS fixes. Safe to call when not tracking.
     */
    fun stop() {

        if (!isTracking) {
            return
        }

        fusedLocationClient.removeLocationUpdates(
            locationCallback
        )

        isTracking = false
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

    private fun Location.toOtoLocation(): OtoLocation =
        OtoLocation(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracy,
            altitudeMeters =
                if (hasAltitude()) altitude else null,
            timestampMillis = time
        )

    companion object {

        private const val DEFAULT_INTERVAL_MILLIS = 3000L

        private const val MIN_INTERVAL_MILLIS = 1000L
    }
}