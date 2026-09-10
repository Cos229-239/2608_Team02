package com.cos229239.team02.oto.ui.components.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Blue marker = Starting Point
 * Green marker = Destination
 * Purple marker = Current Device Location
 *
 * Blue line = Selected Route
 * Gray lines = Alternative Routes
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

    selectedRouteIndex: Int = 0,

    /*
     * Called when the My Location button is pressed.
     *
     * ExplorerScreen handles permission and requests
     * a fresh high-accuracy location.
     */
    onMyLocationClick: () -> Unit = {},

    /*
     * Explorer increments this after a fresh location
     * has been successfully received.
     *
     * That tells the map to center on the new location.
     */
    locationFocusRequest: Int = 0
) {

    val cameraState =
        rememberCameraState()

    /*
     * ---------------------------------------------------------
     * BASIC MAP STATE
     * ---------------------------------------------------------
     */

    val hasSavedTrip =
        startingLatitude != null &&
                startingLongitude != null &&
                destinationLatitude != null &&
                destinationLongitude != null

    val selectedRoute =
        routes.getOrNull(
            selectedRouteIndex
        )

    /*
     * Prevent the route from repeatedly snapping
     * the camera back after the user manually pans.
     */
    var lastFramedRouteKey by remember {
        mutableStateOf<String?>(null)
    }

    /*
     * Prevent continuous GPS updates from repeatedly
     * recentering the map when no trip exists.
     */
    var hasCenteredOnInitialLocation by remember {
        mutableStateOf(false)
    }

    val routeKey =
        if (
            selectedRoute != null &&
            selectedRoute.coordinates.isNotEmpty()
        ) {

            "$selectedRouteIndex-" +
                    "${selectedRoute.coordinates.firstOrNull()}-" +
                    "${selectedRoute.coordinates.lastOrNull()}-" +
                    "${selectedRoute.coordinates.size}"

        } else {

            null
        }

    /*
     * ---------------------------------------------------------
     * FULL ROUTE CAMERA
     * ---------------------------------------------------------
     */

    fun frameFullRoute() {

        val route =
            selectedRoute

        if (
            route != null &&
            route.coordinates.isNotEmpty()
        ) {

            val validCoordinates =
                route.coordinates.filter { coordinate ->
                    coordinate.size >= 2
                }

            if (
                validCoordinates.isNotEmpty()
            ) {

                val longitudes =
                    validCoordinates.map { coordinate ->
                        coordinate[0]
                    }

                val latitudes =
                    validCoordinates.map { coordinate ->
                        coordinate[1]
                    }

                val minimumLongitude =
                    longitudes.minOrNull()

                val maximumLongitude =
                    longitudes.maxOrNull()

                val minimumLatitude =
                    latitudes.minOrNull()

                val maximumLatitude =
                    latitudes.maxOrNull()

                if (
                    minimumLongitude != null &&
                    maximumLongitude != null &&
                    minimumLatitude != null &&
                    maximumLatitude != null
                ) {

                    val centerLongitude =
                        (
                                minimumLongitude +
                                        maximumLongitude
                                ) / 2.0

                    val centerLatitude =
                        (
                                minimumLatitude +
                                        maximumLatitude
                                ) / 2.0

                    val longitudeDifference =
                        abs(
                            maximumLongitude -
                                    minimumLongitude
                        )

                    val latitudeDifference =
                        abs(
                            maximumLatitude -
                                    minimumLatitude
                        )

                    val largestDifference =
                        maxOf(
                            longitudeDifference,
                            latitudeDifference
                        )

                    val routeZoom =
                        when {

                            largestDifference < 0.003 ->
                                16.0

                            largestDifference < 0.008 ->
                                15.0

                            largestDifference < 0.015 ->
                                14.0

                            largestDifference < 0.03 ->
                                13.0

                            largestDifference < 0.06 ->
                                12.0

                            largestDifference < 0.10 ->
                                11.0

                            largestDifference < 0.20 ->
                                10.0

                            largestDifference < 0.40 ->
                                9.0

                            largestDifference < 0.75 ->
                                8.0

                            largestDifference < 1.50 ->
                                7.0

                            largestDifference < 3.0 ->
                                6.0

                            largestDifference < 6.0 ->
                                5.0

                            else ->
                                4.0
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
                                routeZoom
                        )
                }
            }

        } else if (
            hasSavedTrip
        ) {

            /*
             * Fallback before actual route geometry loads.
             */
            val startLat =
                startingLatitude!!

            val startLon =
                startingLongitude!!

            val endLat =
                destinationLatitude!!

            val endLon =
                destinationLongitude!!

            val centerLatitude =
                (
                        startLat +
                                endLat
                        ) / 2.0

            val centerLongitude =
                (
                        startLon +
                                endLon
                        ) / 2.0

            val largestDifference =
                maxOf(
                    abs(
                        startLat -
                                endLat
                    ),

                    abs(
                        startLon -
                                endLon
                    )
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

                    else ->
                        4.0
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
        }
    }

    /*
     * ---------------------------------------------------------
     * AUTO-FRAME ROUTE ONCE
     * ---------------------------------------------------------
     */

    LaunchedEffect(
        routeKey
    ) {

        if (
            routeKey != null &&
            routeKey != lastFramedRouteKey
        ) {

            frameFullRoute()

            lastFramedRouteKey =
                routeKey
        }
    }

    /*
     * ---------------------------------------------------------
     * INITIAL CURRENT LOCATION
     * ---------------------------------------------------------
     *
     * Only centers once if there is no saved trip.
     *
     * Continuous GPS updates will NOT keep moving
     * the camera afterward.
     */
    LaunchedEffect(
        latitude,
        longitude,
        hasSavedTrip
    ) {

        if (
            !hasSavedTrip &&
            !hasCenteredOnInitialLocation &&
            latitude != null &&
            longitude != null
        ) {

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
                        15.0
                )

            hasCenteredOnInitialLocation =
                true
        }
    }

    /*
     * ---------------------------------------------------------
     * FOCUS ON FRESH LOCATION
     * ---------------------------------------------------------
     *
     * Runs only after ExplorerScreen successfully
     * obtains a fresh location from Android.
     */
    LaunchedEffect(
        locationFocusRequest
    ) {

        if (
            locationFocusRequest > 0 &&
            latitude != null &&
            longitude != null
        ) {

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
        }
    }

    /*
     * ---------------------------------------------------------
     * MAP
     * ---------------------------------------------------------
     */

    Box(
        modifier =
            modifier.fillMaxSize()
    ) {

        MaplibreMap(
            modifier =
                Modifier.fillMaxSize(),

            baseStyle =
                BaseStyle.Uri(
                    "https://tiles.openfreemap.org/styles/liberty"
                ),

            cameraState =
                cameraState
        ) {

            /*
             * -------------------------------------------------
             * ALTERNATIVE ROUTES
             * -------------------------------------------------
             */

            routes.forEachIndexed { index, route ->

                if (
                    index != selectedRouteIndex &&
                    route.coordinates.size >= 2
                ) {

                    val positions =
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
                        positions.size >= 2
                    ) {

                        val source =
                            rememberGeoJsonSource(
                                GeoJsonData.Features(
                                    LineString(
                                        positions
                                    )
                                )
                            )

                        LineLayer(
                            id =
                                "oto-route-alternative-$index",

                            source =
                                source,

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
                                    0.70f
                                )
                        )
                    }
                }
            }

            /*
             * -------------------------------------------------
             * SELECTED ROUTE
             * -------------------------------------------------
             */

            if (
                selectedRoute != null &&
                selectedRoute.coordinates.size >= 2
            ) {

                val positions =
                    selectedRoute.coordinates
                        .mapNotNull { coordinate ->

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
                    positions.size >= 2
                ) {

                    val source =
                        rememberGeoJsonSource(
                            GeoJsonData.Features(
                                LineString(
                                    positions
                                )
                            )
                        )

                    LineLayer(
                        id =
                            "oto-route-selected",

                        source =
                            source,

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
             * -------------------------------------------------
             * SAVED START AND DESTINATION
             * -------------------------------------------------
             */

            if (
                hasSavedTrip
            ) {

                val startSource =
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
                 * BLUE = Start
                 */
                CircleLayer(
                    id =
                        "oto-trip-start",

                    source =
                        startSource,

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
                 * GREEN = Destination
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
            }

            /*
             * -------------------------------------------------
             * LIVE CURRENT LOCATION
             * -------------------------------------------------
             */

            if (
                latitude != null &&
                longitude != null
            ) {

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

                /*
                 * Purple distinguishes current GPS
                 * location from saved trip Start.
                 */
                CircleLayer(
                    id =
                        "oto-current-location",

                    source =
                        currentLocationSource,

                    radius =
                        const(
                            8.dp
                        ),

                    color =
                        const(
                            Color(
                                0xFF7E57C2
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

        /*
         * -----------------------------------------------------
         * SMALL MAP CONTROLS
         * -----------------------------------------------------
         */

        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .padding(
                        10.dp
                    )
        ) {

            /*
             * ZOOM IN
             */
            Button(
                onClick = {

                    val currentPosition =
                        cameraState.position

                    cameraState.position =
                        CameraPosition(
                            target =
                                currentPosition.target,

                            zoom =
                                (
                                        currentPosition.zoom +
                                                1.0
                                        ).coerceAtMost(
                                        20.0
                                    )
                        )
                },

                modifier =
                    Modifier.size(
                        42.dp
                    ),

                shape =
                    CircleShape,

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color.White,

                        contentColor =
                            Color(
                                0xFF063D24
                            )
                    ),

                contentPadding =
                    PaddingValues(
                        0.dp
                    )
            ) {

                Text(
                    text =
                        "+",

                    fontSize =
                        22.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.size(
                        6.dp
                    )
            )

            /*
             * ZOOM OUT
             */
            Button(
                onClick = {

                    val currentPosition =
                        cameraState.position

                    cameraState.position =
                        CameraPosition(
                            target =
                                currentPosition.target,

                            zoom =
                                (
                                        currentPosition.zoom -
                                                1.0
                                        ).coerceAtLeast(
                                        2.0
                                    )
                        )
                },

                modifier =
                    Modifier.size(
                        42.dp
                    ),

                shape =
                    CircleShape,

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color.White,

                        contentColor =
                            Color(
                                0xFF063D24
                            )
                    ),

                contentPadding =
                    PaddingValues(
                        0.dp
                    )
            ) {

                Text(
                    text =
                        "−",

                    fontSize =
                        22.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.size(
                        6.dp
                    )
            )

            /*
             * MY LOCATION
             *
             * ExplorerScreen requests a fresh
             * high-accuracy GPS location.
             */
            Button(
                onClick =
                    onMyLocationClick,

                modifier =
                    Modifier.size(
                        42.dp
                    ),

                shape =
                    CircleShape,

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color.White,

                        contentColor =
                            Color(
                                0xFF063D24
                            )
                    ),

                contentPadding =
                    PaddingValues(
                        0.dp
                    )
            ) {

                Text(
                    text =
                        "◎",

                    fontSize =
                        22.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.size(
                        6.dp
                    )
            )

            /*
             * FULL ROUTE
             */
            Button(
                onClick = {
                    frameFullRoute()
                },

                modifier =
                    Modifier.size(
                        42.dp
                    ),

                shape =
                    CircleShape,

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color.White,

                        contentColor =
                            Color(
                                0xFF063D24
                            )
                    ),

                contentPadding =
                    PaddingValues(
                        0.dp
                    )
            ) {

                Text(
                    text =
                        "⌖",

                    fontSize =
                        20.sp
                )
            }
        }
    }
}