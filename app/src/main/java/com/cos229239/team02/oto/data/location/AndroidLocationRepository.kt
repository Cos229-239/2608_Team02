package com.cos229239.team02.oto.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/*
 * Android implementation of LocationRepository.
 *
 * Uses Google's Fused Location Provider to obtain
 * device location.
 *
 * Permission requests are handled by the UI layer.
 */
class AndroidLocationRepository(
    private val context: Context
) : LocationRepository {

    private val fusedLocationClient =
        LocationServices
            .getFusedLocationProviderClient(
                context
            )

    /*
     * ---------------------------------------------------------
     * LAST KNOWN LOCATION
     * ---------------------------------------------------------
     */

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): OtoLocation? {

        if (
            !hasLocationPermission()
        ) {
            return null
        }

        return awaitLocation(
            fusedLocationClient.lastLocation
        )?.toOtoLocation()
    }

    /*
     * ---------------------------------------------------------
     * CURRENT LOCATION
     * ---------------------------------------------------------
     */

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): OtoLocation? {

        if (
            !hasLocationPermission()
        ) {
            return null
        }

        val task =
            fusedLocationClient
                .getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                )

        return awaitLocation(
            task
        )?.toOtoLocation()
    }

    /*
     * ---------------------------------------------------------
     * CONTINUOUS LOCATION UPDATES
     * ---------------------------------------------------------
     *
     * Emits new GPS coordinates while the collector
     * is active.
     *
     * Explorer can collect this Flow and pass each
     * updated location into OtoMap.
     */
    @SuppressLint("MissingPermission")
    fun observeLocationUpdates(): Flow<OtoLocation> =
        callbackFlow {

            /*
             * Do not attempt location updates without
             * Android permission.
             */
            if (
                !hasLocationPermission()
            ) {

                close()

                return@callbackFlow
            }

            /*
             * Request high-accuracy location updates.
             *
             * 2 seconds is frequent enough for navigation
             * testing without being extremely aggressive.
             */
            val locationRequest =
                LocationRequest
                    .Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        2000L
                    )
                    .setMinUpdateIntervalMillis(
                        1000L
                    )
                    .setMinUpdateDistanceMeters(
                        2f
                    )
                    .build()

            /*
             * Receives location updates from Google Play
             * Services.
             */
            val locationCallback =
                object : LocationCallback() {

                    override fun onLocationResult(
                        locationResult: LocationResult
                    ) {

                        val location =
                            locationResult.lastLocation
                                ?: return

                        trySend(
                            location.toOtoLocation()
                        )
                    }
                }

            /*
             * Begin receiving location updates.
             */
            fusedLocationClient
                .requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    context.mainLooper
                )

            /*
             * When Explorer stops collecting the Flow,
             * stop GPS updates to avoid wasting battery.
             */
            awaitClose {

                fusedLocationClient
                    .removeLocationUpdates(
                        locationCallback
                    )
            }
        }

    /*
     * ---------------------------------------------------------
     * PERMISSION CHECK
     * ---------------------------------------------------------
     */

    private fun hasLocationPermission(): Boolean {

        val fineLocationGranted =
            ContextCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ) ==
                    PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted =
            ContextCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                ) ==
                    PackageManager.PERMISSION_GRANTED

        return (
                fineLocationGranted ||
                        coarseLocationGranted
                )
    }

    /*
     * ---------------------------------------------------------
     * TASK → COROUTINE
     * ---------------------------------------------------------
     */

    private suspend fun awaitLocation(
        task: Task<Location>
    ): Location? =
        suspendCancellableCoroutine { continuation ->

            task.addOnSuccessListener { location ->

                if (
                    continuation.isActive
                ) {

                    continuation.resume(
                        location
                    )
                }
            }

            task.addOnFailureListener {

                if (
                    continuation.isActive
                ) {

                    continuation.resume(
                        null
                    )
                }
            }

            task.addOnCanceledListener {

                if (
                    continuation.isActive
                ) {

                    continuation.resume(
                        null
                    )
                }
            }
        }

    /*
     * ---------------------------------------------------------
     * ANDROID LOCATION → OTO LOCATION
     * ---------------------------------------------------------
     */

    private fun Location.toOtoLocation(): OtoLocation =
        OtoLocation(
            latitude =
                latitude,

            longitude =
                longitude,

            accuracyMeters =
                accuracy,

            altitudeMeters =
                if (
                    hasAltitude()
                ) {
                    altitude
                } else {
                    null
                },

            timestampMillis =
                time
        )
}