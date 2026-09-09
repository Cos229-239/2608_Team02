package com.cos229239.team02.oto.ui.components.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cos229239.team02.oto.data.route.RouteResult
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.math.abs

/**
 * Shared interactive map used throughout OTO.
 *
 * Saved trip:
 * Blue = Starting Point
 * Green = Destination
 *
 * Route Navigation:
 * Primary / selected route = highlighted
 * Alternative routes = lighter background routes
 */
@Composable
fun OtoMap(
    modifier: Modifier = Modifier,

    latitude: Double? = null,
    longitude: Double? = null,

    startingLatitude: Double? = null,
    startingLongitude: Double? = null,

    destinationLatitude: Double? = null,
    destinationLongitude: Double? = null,

    routes: List<RouteResult> = emptyList(),

    selectedRouteIndex: Int = 0
) {

    val cameraState =
        rememberCameraState()

    /*
     * Determine whether Explorer has a complete
     * saved trip available.
     */
    val hasSavedTrip =
        startingLatitude != null &&
                startingLongitude != null &&
                destinationLatitude != null &&
                destinationLongitude != null

    /*
     * ---------------------------------------------------------
     * CAMERA POSITION
     * ---------------------------------------------------------
     */

    LaunchedEffect(
        latitude,
        longitude,
        startingLatitude,
        startingLongitude,
        destinationLatitude,
        destinationLongitude
    ) {

        /*
         * Saved trip takes priority.
         */
        if (hasSavedTrip) {

            val startLat =
                startingLatitude!!

            val startLon =
                startingLongitude!!

            val destinationLat =
                destinationLatitude!!

            val destinationLon =
                destinationLongitude!!

            /*
             * Center the camera between the
             * starting point and destination.
             */
            val centerLatitude =
                (
                        startLat +
                                destinationLat
                        ) / 2.0

            val centerLongitude =
                (
                        startLon +
                                destinationLon
                        ) / 2.0

            /*
             * Choose a zoom level based on
             * the distance between the two points.
             */
            val latitudeDifference =
                abs(
                    startLat -
                            destinationLat
                )

            val longitudeDifference =
                abs(
                    startLon -
                            destinationLon
                )

            val largestDifference =
                maxOf(
                    latitudeDifference,
                    longitudeDifference
                )

            val tripZoom =
                when {

                    largestDifference < 0.01 ->
                        14.0

                    largestDifference < 0.03 ->
                        12.5

                    largestDifference < 0.08 ->
                        11.0

                    largestDifference < 0.20 ->
                        9.5

                    largestDifference < 0.50 ->
                        8.0

                    largestDifference < 1.0 ->
                        7.0

                    largestDifference < 3.0 ->
                        5.5

                    largestDifference < 7.0 ->
                        4.5

                    else ->
                        3.5
                }

            cameraState.position =
                CameraPosition(
                    target =
                        Position(
                            longitude =
                                centerLongitude,

                            latitude =
                                centerLatitude
                        ),

                    zoom =
                        tripZoom
                )

        } else if (
            latitude != null &&
            longitude != null
        ) {

            /*
             * No saved trip.
             *
             * Fall back to current GPS location.
             */
            cameraState.position =
                CameraPosition(
                    target =
                        Position(
                            longitude =
                                longitude,

                            latitude =
                                latitude
                        ),

                    zoom =
                        14.0
                )
        }
    }

    /*
     * ---------------------------------------------------------
     * MAP
     * ---------------------------------------------------------
     */

    MaplibreMap(
        modifier =
            modifier.fillMaxSize(),

        baseStyle =
            BaseStyle.Uri(
                "https://tiles.openfreemap.org/styles/liberty"
            ),

        cameraState =
            cameraState
    ) {

        /*
         * -----------------------------------------------------
         * ALTERNATIVE ROUTES
         * -----------------------------------------------------
         *
         * Draw alternatives first so the selected route
         * appears on top of them.
         */

        routes.forEachIndexed { index, route ->

            if (
                index != selectedRouteIndex &&
                route.coordinates.size >= 2
            ) {

                val alternativePositions =
                    route.coordinates.mapNotNull { coordinate ->

                        if (
                            coordinate.size >= 2
                        ) {

                            Position(
                                longitude =
                                    coordinate[0],

                                latitude =
                                    coordinate[1]
                            )

                        } else {

                            null
                        }
                    }

                if (
                    alternativePositions.size >= 2
                ) {

                    val alternativeSource =
                        rememberGeoJsonSource(
                            GeoJsonData.Features(
                                LineString(
                                    alternativePositions
                                )
                            )
                        )

                    /*
                     * Alternative routes are visible,
                     * but intentionally less prominent.
                     */
                    LineLayer(
                        id =
                            "oto-route-alternative-$index",

                        source =
                            alternativeSource,

                        color =
                            const(
                                Color(
                                    0xFF9E9E9E
                                )
                            ),

                        width =
                            const(
                                4.dp
                            ),

                        opacity =
                            const(
                                0.75f
                            )
                    )
                }
            }
        }

        /*
         * -----------------------------------------------------
         * SELECTED / PRIMARY ROUTE
         * -----------------------------------------------------
         */

        val selectedRoute =
            routes.getOrNull(
                selectedRouteIndex
            )

        if (
            selectedRoute != null &&
            selectedRoute.coordinates.size >= 2
        ) {

            val selectedPositions =
                selectedRoute.coordinates.mapNotNull { coordinate ->

                    if (
                        coordinate.size >= 2
                    ) {

                        Position(
                            longitude =
                                coordinate[0],

                            latitude =
                                coordinate[1]
                        )

                    } else {

                        null
                    }
                }

            if (
                selectedPositions.size >= 2
            ) {

                val selectedRouteSource =
                    rememberGeoJsonSource(
                        GeoJsonData.Features(
                            LineString(
                                selectedPositions
                            )
                        )
                    )

                /*
                 * Main route.
                 *
                 * Blue is used here because it visually
                 * connects with the starting point.
                 */
                LineLayer(
                    id =
                        "oto-route-selected",

                    source =
                        selectedRouteSource,

                    color =
                        const(
                            Color(
                                0xFF1976D2
                            )
                        ),

                    width =
                        const(
                            6.dp
                        ),

                    opacity =
                        const(
                            1.0f
                        )
                )
            }
        }

        /*
         * -----------------------------------------------------
         * SAVED TRIP MARKERS
         * -----------------------------------------------------
         */

        if (hasSavedTrip) {

            /*
             * Starting Point
             */
            val startingPointSource =
                rememberGeoJsonSource(
                    GeoJsonData.Features(
                        Point(
                            Position(
                                longitude =
                                    startingLongitude!!,

                                latitude =
                                    startingLatitude!!
                            )
                        )
                    )
                )

            /*
             * BLUE = Starting Point
             */
            CircleLayer(
                id =
                    "oto-trip-start",

                source =
                    startingPointSource,

                radius =
                    const(
                        9.dp
                    ),

                color =
                    const(
                        Color(
                            0xFF1976D2
                        )
                    ),

                strokeColor =
                    const(
                        Color.White
                    ),

                strokeWidth =
                    const(
                        3.dp
                    )
            )

            /*
             * Destination
             */
            val destinationSource =
                rememberGeoJsonSource(
                    GeoJsonData.Features(
                        Point(
                            Position(
                                longitude =
                                    destinationLongitude!!,

                                latitude =
                                    destinationLatitude!!
                            )
                        )
                    )
                )

            /*
             * GREEN = Destination / Finish
             */
            CircleLayer(
                id =
                    "oto-trip-destination",

                source =
                    destinationSource,

                radius =
                    const(
                        9.dp
                    ),

                color =
                    const(
                        Color(
                            0xFF149447
                        )
                    ),

                strokeColor =
                    const(
                        Color.White
                    ),

                strokeWidth =
                    const(
                        3.dp
                    )
            )

        } else if (
            latitude != null &&
            longitude != null
        ) {

            /*
             * -------------------------------------------------
             * CURRENT LOCATION
             * -------------------------------------------------
             *
             * Only shown when there is no saved trip.
             */

            val currentLocationSource =
                rememberGeoJsonSource(
                    GeoJsonData.Features(
                        Point(
                            Position(
                                longitude =
                                    longitude,

                                latitude =
                                    latitude
                            )
                        )
                    )
                )

            CircleLayer(
                id =
                    "oto-current-location",

                source =
                    currentLocationSource,

                radius =
                    const(
                        9.dp
                    ),

                color =
                    const(
                        Color(
                            0xFF1976D2
                        )
                    ),

                strokeColor =
                    const(
                        Color.White
                    ),

                strokeWidth =
                    const(
                        3.dp
                    )
            )
        }
    }
}