package com.cos229239.team02.oto.data.route

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Retrieves real driving routes between two saved trip locations.
 *
 * The first returned route is treated as the primary route.
 * Additional returned routes are treated as alternatives.
 */
class RouteClient {

    private val client =
        OkHttpClient.Builder()
            .connectTimeout(
                15,
                TimeUnit.SECONDS
            )
            .readTimeout(
                20,
                TimeUnit.SECONDS
            )
            .build()

    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    /**
     * Requests the primary route plus any available
     * alternative routes.
     */
    suspend fun getRoutes(
        startingLatitude: Double,
        startingLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double
    ): List<RouteResult> {

        /*
         * OSRM expects:
         *
         * longitude,latitude
         *
         * NOT latitude,longitude.
         */
        val url =
            "https://router.project-osrm.org/route/v1/driving/" +
                    "$startingLongitude,$startingLatitude;" +
                    "$destinationLongitude,$destinationLatitude" +
                    "?overview=full" +
                    "&geometries=geojson" +
                    "&steps=false" +
                    "&alternatives=true"

        val request =
            Request.Builder()
                .url(url)
                .header(
                    "User-Agent",
                    "OTO-App/1.0"
                )
                .get()
                .build()

        return try {

            withContext(
                Dispatchers.IO
            ) {

                client
                    .newCall(
                        request
                    )
                    .execute()
                    .use { response ->

                        if (
                            !response.isSuccessful
                        ) {
                            return@withContext emptyList()
                        }

                        val responseBody =
                            response
                                .body
                                ?.string()
                                .orEmpty()

                        val routeResponse =
                            json.decodeFromString<OsrmRouteResponse>(
                                responseBody
                            )

                        if (
                            routeResponse.code !=
                            "Ok"
                        ) {
                            return@withContext emptyList()
                        }

                        routeResponse.routes.mapIndexed { index, route ->

                            RouteResult(
                                routeIndex =
                                    index,

                                coordinates =
                                    route
                                        .geometry
                                        .coordinates,

                                distanceMeters =
                                    route.distance,

                                durationSeconds =
                                    route.duration,

                                isPrimary =
                                    index == 0
                            )
                        }
                    }
            }

        } catch (
            error: CancellationException
        ) {

            throw error

        } catch (
            error: Exception
        ) {

            /*
             * Network/API failure.
             */
            emptyList()
        }
    }
}


/**
 * Route data used by Explorer and OtoMap.
 */
data class RouteResult(

    /*
     * 0 = primary route
     * 1+ = alternative routes
     */
    val routeIndex: Int,

    /*
     * Each coordinate is:
     *
     * [longitude, latitude]
     */
    val coordinates: List<List<Double>>,

    val distanceMeters: Double,

    val durationSeconds: Double,

    /*
     * True only for the first route returned.
     */
    val isPrimary: Boolean
)


/*
 * -------------------------------------------------------------
 * OSRM RESPONSE MODELS
 * -------------------------------------------------------------
 */

@Serializable
private data class OsrmRouteResponse(
    val code: String,
    val routes: List<OsrmRoute> = emptyList()
)

@Serializable
private data class OsrmRoute(
    val distance: Double,
    val duration: Double,
    val geometry: OsrmGeometry
)

@Serializable
private data class OsrmGeometry(
    val type: String,
    val coordinates: List<List<Double>>
)