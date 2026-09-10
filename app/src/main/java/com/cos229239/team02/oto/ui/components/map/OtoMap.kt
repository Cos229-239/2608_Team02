package com.cos229239.team02.oto.ui.components.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cos229239.team02.oto.data.location.OtoLocation
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
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

//The style OTO's offline regions must match so maps work offline.
const val OTO_MAP_STYLE_URL =
    "https://tiles.openfreemap.org/styles/liberty"

/**
 * Shared interactive map used throughout OTO.
 *
 * If a saved trip exists, the map displays:
 * Blue = Starting Point
 * Green = Destination
 *
 * If there is no saved trip, the map can display
 * the user's current location instead.
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

    routePoints: List<OtoLocation> = emptyList(),
    followCamera: Boolean = true
) {

    val cameraState =
        rememberCameraState()

    var hasCenteredOnce by remember { mutableStateOf(false) }

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

        if (hasSavedTrip) {

            val startLat =
                startingLatitude!!

            val startLon =
                startingLongitude!!

            val destinationLat =
                destinationLatitude!!

            val destinationLon =
                destinationLongitude!!

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

            if (
                !hasCenteredOnce
            ) {

                hasCenteredOnce = true

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
                            17.0
                    )

            } else if (followCamera) {

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
                            cameraState.position.zoom
                    )
            }
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
                OTO_MAP_STYLE_URL
            ),

        cameraState =
            cameraState
    ) {

        /*
         * -----------------------------------------------------
         * SAVED TRIP MARKERS
         * -----------------------------------------------------
         */

        if (hasSavedTrip) {

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
             * Current location is only shown when
             * no saved trip exists.
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

        /*
         * -----------------------------------------------------
         * TRACKED / BACKTRACK ROUTE LINE
         * -----------------------------------------------------
         */

        if (routePoints.size >= 2) {

            val routeSource =
                rememberGeoJsonSource(
                    GeoJsonData.Features(
                        LineString(
                            routePoints.map { point ->
                                Position(
                                    longitude =
                                        point.longitude,

                                    latitude =
                                        point.latitude
                                )
                            }
                        )
                    )
                )

            LineLayer(
                id =
                    "oto-route-polyline",

                source =
                    routeSource,

                cap =
                    const(
                        LineCap.Round
                    ),

                join =
                    const(
                        LineJoin.Round
                    ),

                width =
                    const(
                        4.dp
                    ),

                color =
                    const(
                        Color(
                            0xFF1976D2
                        )
                    )
            )
        }
    }
}