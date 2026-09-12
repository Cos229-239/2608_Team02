package com.cos229239.team02.oto.ui.components.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cos229239.team02.oto.data.hazard.HazardPriority
import com.cos229239.team02.oto.data.hazard.HazardReport
import com.cos229239.team02.oto.data.location.OtoLocation
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
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.math.abs

// The style OTO's offline regions must match so maps work offline.
const val OTO_MAP_STYLE_URL =
    "https://tiles.openfreemap.org/styles/liberty"

/*
 * -------------------------------------------------------------
 * HAZARD MAP CLUSTER
 * -------------------------------------------------------------
 *
 * Nearby reports from the same broad category
 * are grouped into one map marker.
 *
 * Example:
 *
 * Bear Sighting
 * Coyote Sighting
 *
 * become one Wildlife marker when they are
 * reported close to each other.
 */
private data class HazardMapCluster(
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val reports: List<HazardReport>
)

/*
 * Reports closer than roughly this latitude /
 * longitude difference can be grouped.
 *
 * This is intentionally simple for the prototype.
 */
private const val HAZARD_CLUSTER_DISTANCE =
    0.01

/**
 * Shared interactive map used throughout OTO.
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
     * Active hazard reports shown on the map.
     */
    hazardReports: List<HazardReport> = emptyList(),

    /*
     * Called when the user chooses to open the
     * reports represented by a hazard marker.
     */
    onViewHazardReportsClick:
        (List<HazardReport>) -> Unit = {},

    /*
     * Called when the My Location button is pressed.
     */
    onMyLocationClick: () -> Unit = {},

    /*
     * Explorer increments this after receiving
     * a fresh location.
     */
    locationFocusRequest: Int = 0,

    /*
     * Recorded route points used by Backtrack.
     */
    routePoints: List<OtoLocation> = emptyList(),

    /*
     * Backtrack follow-camera mode.
     */
    followCamera: Boolean = true,

    /*
     * Explorer shows the location button.
     */
    showMyLocationButton: Boolean = true
) {

    val cameraState =
        rememberCameraState()

    /*
     * ---------------------------------------------------------
     * MAP STATE
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

    var lastFramedRouteKey by remember {
        mutableStateOf<String?>(null)
    }

    var hasCenteredOnInitialLocation by remember {
        mutableStateOf(false)
    }

    var hasFollowCenteredOnce by remember {
        mutableStateOf(false)
    }

    /*
     * Selected hazard marker.
     */
    var selectedHazardCluster by remember {
        mutableStateOf<HazardMapCluster?>(null)
    }

    /*
     * ---------------------------------------------------------
     * GROUP HAZARDS
     * ---------------------------------------------------------
     */

    val hazardClusters =
        buildHazardClusters(
            hazardReports.filter {
                it.isActive
            }
        )

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
     * FRAME SAVED TRIP
     * ---------------------------------------------------------
     */

    fun frameSavedTrip() {

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
                    validCoordinates.map {
                        it[0]
                    }

                val latitudes =
                    validCoordinates.map {
                        it[1]
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

            frameSavedTrip()
        }
    }

    /*
     * ---------------------------------------------------------
     * AUTO FRAME ROUTE
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
     * FALLBACK SAVED TRIP FRAME
     * ---------------------------------------------------------
     */

    LaunchedEffect(
        hasSavedTrip,
        startingLatitude,
        startingLongitude,
        destinationLatitude,
        destinationLongitude,
        routeKey
    ) {

        if (
            hasSavedTrip &&
            routeKey == null
        ) {

            frameSavedTrip()
        }
    }

    /*
     * ---------------------------------------------------------
     * INITIAL CURRENT LOCATION
     * ---------------------------------------------------------
     */

    LaunchedEffect(
        latitude,
        longitude,
        hasSavedTrip
    ) {

        if (
            !hasSavedTrip &&
            routePoints.isEmpty() &&
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
     * FOLLOW CAMERA
     * ---------------------------------------------------------
     */

    LaunchedEffect(
        latitude,
        longitude,
        routePoints,
        followCamera
    ) {

        if (
            routePoints.isNotEmpty() &&
            followCamera &&
            latitude != null &&
            longitude != null
        ) {

            if (
                !hasFollowCenteredOnce
            ) {

                hasFollowCenteredOnce =
                    true

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

            } else {

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
     * LOCATION BUTTON CAMERA
     * ---------------------------------------------------------
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
                    OTO_MAP_STYLE_URL
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
             * TRACKED / BACKTRACK ROUTE
             * -------------------------------------------------
             */

            if (
                routePoints.size >= 2
            ) {

                val routePositions =
                    routePoints.map { point ->

                        Position(
                            longitude =
                                point.longitude,

                            latitude =
                                point.latitude
                        )
                    }

                val routeSource =
                    rememberGeoJsonSource(
                        GeoJsonData.Features(
                            LineString(
                                routePositions
                            )
                        )
                    )

                LineLayer(
                    id =
                        "oto-route-polyline",

                    source =
                        routeSource,

                    color =
                        const(
                            Color(
                                0xFF1976D2
                            )
                        ),

                    width =
                        const(
                            4.dp
                        ),

                    opacity =
                        const(
                            0.90f
                        )
                )
            }

            /*
             * -------------------------------------------------
             * START AND DESTINATION
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
             * GROUPED HAZARD MARKERS
             * -------------------------------------------------
             */

            hazardClusters.forEachIndexed { index, cluster ->

                val clusterSource =
                    rememberGeoJsonSource(
                        GeoJsonData.Features(
                            Point(
                                Position(
                                    longitude =
                                        cluster.longitude,

                                    latitude =
                                        cluster.latitude
                                )
                            )
                        )
                    )

                /*
                 * Category controls marker color.
                 */
                val categoryColor =
                    hazardCategoryColor(
                        cluster.category
                    )

                /*
                 * Highest priority inside the cluster
                 * controls marker size and border.
                 */
                val highestPriority =
                    highestPriority(
                        cluster.reports
                    )

                val markerRadius =
                    when (
                        highestPriority
                    ) {

                        HazardPriority.CRITICAL ->
                            21.dp

                        HazardPriority.HIGH ->
                            18.dp

                        HazardPriority.NORMAL ->
                            16.dp
                    }

                val borderWidth =
                    when (
                        highestPriority
                    ) {

                        HazardPriority.CRITICAL ->
                            5.dp

                        HazardPriority.HIGH ->
                            4.dp

                        HazardPriority.NORMAL ->
                            3.dp
                    }

                CircleLayer(
                    id =
                        "oto-hazard-cluster-$index",

                    source =
                        clusterSource,

                    radius =
                        const(
                            markerRadius
                        ),

                    color =
                        const(
                            categoryColor
                        ),

                    strokeColor =
                        const(
                            if (
                                highestPriority ==
                                HazardPriority.CRITICAL
                            ) {

                                Color(
                                    0xFFB00020
                                )

                            } else {

                                Color.White
                            }
                        ),

                    strokeWidth =
                        const(
                            borderWidth
                        ),

                    /*
                     * Tap the marker to show
                     * the report summary popup.
                     */
                    onClick = {

                        selectedHazardCluster =
                            cluster

                        ClickResult.Consume
                    }
                )
            }

            /*
             * -------------------------------------------------
             * CURRENT LOCATION
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
                 * Purple current-location dot stays smaller
                 * so a hazard underneath remains visible.
                 */
                CircleLayer(
                    id =
                        "oto-current-location",

                    source =
                        currentLocationSource,

                    radius =
                        const(
                            7.dp
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
                            2.dp
                        )
                )
            }
        }

        /*
         * -----------------------------------------------------
         * SELECTED HAZARD POPUP
         * -----------------------------------------------------
         */

        selectedHazardCluster
            ?.let { cluster ->

                HazardClusterCard(
                    cluster =
                        cluster,

                    modifier =
                        Modifier
                            .align(
                                Alignment.TopStart
                            )
                            .fillMaxWidth()
                            .padding(
                                start =
                                    12.dp,

                                top =
                                    12.dp,

                                end =
                                    72.dp
                            ),

                    onCloseClick = {

                        selectedHazardCluster =
                            null
                    },

                    onViewReportsClick = {

                        onViewHazardReportsClick(
                            cluster.reports
                        )
                    }
                )
            }

        /*
         * -----------------------------------------------------
         * MAP CONTROLS
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
             */
            if (
                showMyLocationButton
            ) {

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
            }

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


/*
 * -------------------------------------------------------------
 * HAZARD CLUSTER POPUP CARD
 * -------------------------------------------------------------
 */

@Composable
private fun HazardClusterCard(
    cluster: HazardMapCluster,
    modifier: Modifier,
    onCloseClick: () -> Unit,
    onViewReportsClick: () -> Unit
) {

    val categoryColor =
        hazardCategoryColor(
            cluster.category
        )

    Card(
        modifier =
            modifier,

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White.copy(
                        alpha =
                            0.97f
                    )
            ),

        shape =
            RoundedCornerShape(
                14.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    14.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    6.dp
                )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        cluster.category.uppercase(),

                    color =
                        categoryColor,

                    fontSize =
                        12.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                TextButton(
                    onClick =
                        onCloseClick,

                    contentPadding =
                        PaddingValues(
                            0.dp
                        )
                ) {

                    Text(
                        text =
                            "✕",

                        color =
                            Color(
                                0xFF444444
                            )
                    )
                }
            }

            /*
             * Show each different report type
             * inside this marker.
             */
            cluster.reports
                .map {
                    it.reportType
                }
                .distinct()
                .forEach { reportType ->

                    Text(
                        text =
                            "• $reportType",

                        color =
                            Color(
                                0xFF063D24
                            ),

                        fontWeight =
                            FontWeight.Bold
                    )
                }

            Text(
                text =
                    if (
                        cluster.reports.size == 1
                    ) {

                        "1 active report"

                    } else {

                        "${cluster.reports.size} active reports"
                    },

                color =
                    Color(
                        0xFF666666
                    ),

                fontSize =
                    12.sp
            )

            Button(
                onClick =
                    onViewReportsClick,

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(
                                0xFF0B5D1E
                            )
                    )
            ) {

                Text(
                    text =
                        "VIEW FIELD REPORTS",

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


/*
 * -------------------------------------------------------------
 * BUILD HAZARD CLUSTERS
 * -------------------------------------------------------------
 */

private fun buildHazardClusters(
    reports: List<HazardReport>
): List<HazardMapCluster> {

    val clusters =
        mutableListOf<HazardMapCluster>()

    reports.forEach { report ->

        /*
         * Find an existing cluster from the
         * same category that is nearby.
         */
        val existingIndex =
            clusters.indexOfFirst { cluster ->

                cluster.category ==
                        report.category &&
                        abs(
                            cluster.latitude -
                                    report.latitude
                        ) <=
                        HAZARD_CLUSTER_DISTANCE &&
                        abs(
                            cluster.longitude -
                                    report.longitude
                        ) <=
                        HAZARD_CLUSTER_DISTANCE
            }

        if (
            existingIndex == -1
        ) {

            /*
             * First report in this area/category.
             */
            clusters.add(
                HazardMapCluster(
                    category =
                        report.category,

                    latitude =
                        report.latitude,

                    longitude =
                        report.longitude,

                    reports =
                        listOf(
                            report
                        )
                )
            )

        } else {

            val existingCluster =
                clusters[
                    existingIndex
                ]

            val updatedReports =
                existingCluster.reports +
                        report

            /*
             * Average all coordinates so the grouped
             * marker sits roughly in the center.
             */
            val averageLatitude =
                updatedReports
                    .map {
                        it.latitude
                    }
                    .average()

            val averageLongitude =
                updatedReports
                    .map {
                        it.longitude
                    }
                    .average()

            clusters[
                existingIndex
            ] =
                existingCluster.copy(
                    latitude =
                        averageLatitude,

                    longitude =
                        averageLongitude,

                    reports =
                        updatedReports
                )
        }
    }

    return clusters
}


/*
 * -------------------------------------------------------------
 * CATEGORY COLOR
 * -------------------------------------------------------------
 */

private fun hazardCategoryColor(
    category: String
): Color {

    return when (
        category
    ) {

        /*
         * Red:
         * accidents / emergencies
         */
        "Accident / Emergency" ->
            Color(
                0xFFD32F2F
            )

        /*
         * Orange:
         * bears, coyotes, snakes, etc.
         */
        "Wildlife" ->
            Color(
                0xFFF57C00
            )

        /*
         * Brown:
         * trees, plants, rockfall, etc.
         */
        "Nature / Plants" ->
            Color(
                0xFF8D6E36
            )

        /*
         * Yellow:
         * roads / trails
         */
        "Trail / Road Hazard" ->
            Color(
                0xFFFBC02D
            )

        /*
         * Blue:
         * weather / environmental
         */
        "Weather / Environmental" ->
            Color(
                0xFF1976D2
            )

        /*
         * Purple:
         * infrastructure / facilities
         */
        "Facility / Infrastructure" ->
            Color(
                0xFF7E57C2
            )

        /*
         * Gray:
         * anything else
         */
        else ->
            Color(
                0xFF757575
            )
    }
}


/*
 * -------------------------------------------------------------
 * HIGHEST PRIORITY IN CLUSTER
 * -------------------------------------------------------------
 */

private fun highestPriority(
    reports: List<HazardReport>
): HazardPriority {

    return when {

        reports.any {
            it.priority ==
                    HazardPriority.CRITICAL
        } -> {

            HazardPriority.CRITICAL
        }

        reports.any {
            it.priority ==
                    HazardPriority.HIGH
        } -> {

            HazardPriority.HIGH
        }

        else -> {

            HazardPriority.NORMAL
        }
    }
}