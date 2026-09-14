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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Map style used throughout OTO.
const val OTO_MAP_STYLE_URL =
    "https://tiles.openfreemap.org/styles/liberty"

/*
 * -------------------------------------------------------------
 * HAZARD MAP SETTINGS
 * -------------------------------------------------------------
 */

// Same-category reports inside this distance become one marker.
private const val SAME_CATEGORY_CLUSTER_DISTANCE_METERS =
    150.0

/*
 * Different-category markers inside this distance are
 * considered to occupy essentially the same map location.
 */
private const val SPIDER_GROUP_DISTANCE_METERS =
    55.0

/*
 * Distance used only while the user has expanded a
 * spider group.
 *
 * This does NOT change the report's real coordinates.
 */
private const val SPIDER_RADIUS_METERS =
    32.0

/*
 * -------------------------------------------------------------
 * HAZARD MAP CLUSTER
 * -------------------------------------------------------------
 */

private data class HazardMapCluster(
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val reports: List<HazardReport>
)

/*
 * -------------------------------------------------------------
 * SPIDER GROUP
 * -------------------------------------------------------------
 *
 * A SpiderGroup contains multiple different-category
 * clusters that occupy nearly the same location.
 *
 * The center remains the truthful map location.
 */
private data class HazardSpiderGroup(
    val key: String,
    val latitude: Double,
    val longitude: Double,
    val clusters: List<HazardMapCluster>
)

/*
 * -------------------------------------------------------------
 * SPIDER MARKER
 * -------------------------------------------------------------
 *
 * Contains the temporarily offset display coordinate.
 */
private data class SpiderMarker(
    val cluster: HazardMapCluster,
    val displayLatitude: Double,
    val displayLongitude: Double
)

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

    hazardReports: List<HazardReport> = emptyList(),

    /*
     * Returns the exact reports represented by the
     * hazard marker selected by the user.
     */
    onViewHazardReportsClick:
        (List<HazardReport>) -> Unit = {},

    onMyLocationClick: () -> Unit = {},

    locationFocusRequest: Int = 0,

    routePoints: List<OtoLocation> = emptyList(),

    followCamera: Boolean = true,

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
     * Normal hazard popup selection.
     */
    var selectedHazardCluster by remember {
        mutableStateOf<HazardMapCluster?>(null)
    }

    /*
     * Which overlapping marker group is currently expanded.
     *
     * null = all groups collapsed
     */
    var expandedSpiderGroupKey by remember {
        mutableStateOf<String?>(null)
    }

    /*
     * ---------------------------------------------------------
     * HAZARD GROUPING
     * ---------------------------------------------------------
     */

    val hazardClusters =
        buildHazardClusters(
            hazardReports.filter {
                it.isActive
            }
        )

    /*
     * Different-category clusters sharing approximately
     * the same location.
     */
    val spiderGroups =
        buildSpiderGroups(
            hazardClusters
        )

    /*
     * All clusters belonging to a spider group.
     *
     * These should not also be rendered independently.
     */
    val groupedClusterKeys =
        spiderGroups
            .flatMap { group ->

                group.clusters.map { cluster ->
                    hazardClusterKey(
                        cluster
                    )
                }
            }
            .toSet()

    /*
     * Clusters that do not overlap another category
     * are drawn normally.
     */
    val standaloneClusters =
        hazardClusters.filter { cluster ->

            hazardClusterKey(
                cluster
            ) !in
                    groupedClusterKeys
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
             * BACKTRACK ROUTE
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
             * START + DESTINATION
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
             * STANDALONE HAZARD MARKERS
             * -------------------------------------------------
             */

            standaloneClusters.forEachIndexed { index, cluster ->

                HazardCircleLayer(
                    id =
                        "oto-hazard-single-$index",

                    cluster =
                        cluster,

                    latitude =
                        cluster.latitude,

                    longitude =
                        cluster.longitude,

                    onClick = {

                        expandedSpiderGroupKey =
                            null

                        selectedHazardCluster =
                            cluster
                    }
                )
            }

            /*
             * -------------------------------------------------
             * SPIDER GROUPS
             * -------------------------------------------------
             */

            spiderGroups.forEachIndexed { groupIndex, group ->

                val isExpanded =
                    expandedSpiderGroupKey ==
                            group.key

                /*
                 * -------------------------------------------------
                 * COLLAPSED
                 * -------------------------------------------------
                 *
                 * All markers remain at their true location.
                 *
                 * We draw multiple concentric category colors so
                 * the user can immediately see that more than one
                 * category exists here.
                 */

                if (
                    !isExpanded
                ) {

                    val visibleClusters =
                        group.clusters.take(
                            3
                        )

                    /*
                     * Draw largest ring first.
                     */
                    visibleClusters
                        .reversed()
                        .forEachIndexed { ringIndex, cluster ->

                            val source =
                                rememberGeoJsonSource(
                                    GeoJsonData.Features(
                                        Point(
                                            Position(
                                                longitude =
                                                    group.longitude,

                                                latitude =
                                                    group.latitude
                                            )
                                        )
                                    )
                                )

                            val ringRadius =
                                when (
                                    ringIndex
                                ) {

                                    0 ->
                                        24.dp

                                    1 ->
                                        20.dp

                                    else ->
                                        16.dp
                                }

                            CircleLayer(
                                id =
                                    "oto-spider-collapsed-$groupIndex-$ringIndex",

                                source =
                                    source,

                                radius =
                                    const(
                                        ringRadius
                                    ),

                                color =
                                    const(
                                        hazardCategoryColor(
                                            cluster.category
                                        )
                                    ),

                                strokeColor =
                                    const(
                                        Color.White
                                    ),

                                strokeWidth =
                                    const(
                                        3.dp
                                    ),

                                onClick = {

                                    /*
                                     * Expand this stack instead
                                     * of opening a report immediately.
                                     */
                                    selectedHazardCluster =
                                        null

                                    expandedSpiderGroupKey =
                                        group.key

                                    ClickResult.Consume
                                }
                            )
                        }

                } else {

                    /*
                     * -------------------------------------------------
                     * EXPANDED / SPIDERFIED
                     * -------------------------------------------------
                     */

                    val spiderMarkers =
                        createSpiderMarkers(
                            group
                        )

                    /*
                     * Draw lines from the truthful location to the
                     * temporary marker positions.
                     */
                    spiderMarkers.forEachIndexed { markerIndex, marker ->

                        val lineSource =
                            rememberGeoJsonSource(
                                GeoJsonData.Features(
                                    LineString(
                                        listOf(
                                            Position(
                                                longitude =
                                                    group.longitude,

                                                latitude =
                                                    group.latitude
                                            ),

                                            Position(
                                                longitude =
                                                    marker.displayLongitude,

                                                latitude =
                                                    marker.displayLatitude
                                            )
                                        )
                                    )
                                )
                            )

                        LineLayer(
                            id =
                                "oto-spider-line-$groupIndex-$markerIndex",

                            source =
                                lineSource,

                            color =
                                const(
                                    Color(
                                        0xFF5F6368
                                    )
                                ),

                            width =
                                const(
                                    2.dp
                                ),

                            opacity =
                                const(
                                    0.75f
                                )
                        )
                    }

                    /*
                     * Small center marker showing the truthful
                     * hazard location.
                     *
                     * Tapping this collapses the spider.
                     */
                    val centerSource =
                        rememberGeoJsonSource(
                            GeoJsonData.Features(
                                Point(
                                    Position(
                                        longitude =
                                            group.longitude,

                                        latitude =
                                            group.latitude
                                    )
                                )
                            )
                        )

                    CircleLayer(
                        id =
                            "oto-spider-center-$groupIndex",

                        source =
                            centerSource,

                        radius =
                            const(
                                8.dp
                            ),

                        color =
                            const(
                                Color(
                                    0xFF455A64
                                )
                            ),

                        strokeColor =
                            const(
                                Color.White
                            ),

                        strokeWidth =
                            const(
                                2.dp
                            ),

                        onClick = {

                            expandedSpiderGroupKey =
                                null

                            selectedHazardCluster =
                                null

                            ClickResult.Consume
                        }
                    )

                    /*
                     * Draw each category marker at its temporary
                     * spider location.
                     */
                    spiderMarkers.forEachIndexed { markerIndex, marker ->

                        HazardCircleLayer(
                            id =
                                "oto-spider-marker-$groupIndex-$markerIndex",

                            cluster =
                                marker.cluster,

                            latitude =
                                marker.displayLatitude,

                            longitude =
                                marker.displayLongitude,

                            onClick = {

                                selectedHazardCluster =
                                    marker.cluster
                            }
                        )
                    }
                }
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

                        /*
                         * Closing the popup collapses the spider.
                         */
                        expandedSpiderGroupKey =
                            null
                    },

                    onViewReportsClick = {

                        /*
                         * Only send reports belonging to the
                         * selected category marker.
                         */
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
 * HAZARD CIRCLE
 * -------------------------------------------------------------
 *
 * Reusable marker used for both standalone hazards
 * and expanded spider markers.
 */

@Composable
private fun HazardCircleLayer(
    id: String,
    cluster: HazardMapCluster,
    latitude: Double,
    longitude: Double,
    onClick: () -> Unit
) {

    val source =
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

    val categoryColor =
        hazardCategoryColor(
            cluster.category
        )

    val priority =
        highestPriority(
            cluster.reports
        )

    val markerRadius =
        when (
            priority
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
            priority
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
            id,

        source =
            source,

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
                    priority ==
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

        onClick = {

            onClick()

            ClickResult.Consume
        }
    )
}


/*
 * -------------------------------------------------------------
 * HAZARD POPUP
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
 * SAME-CATEGORY CLUSTERING
 * -------------------------------------------------------------
 */

private fun buildHazardClusters(
    reports: List<HazardReport>
): List<HazardMapCluster> {

    val clusters =
        mutableListOf<HazardMapCluster>()

    reports.forEach { report ->

        val existingIndex =
            clusters.indexOfFirst { cluster ->

                cluster.category ==
                        report.category &&
                        distanceMeters(
                            latitude1 =
                                cluster.latitude,

                            longitude1 =
                                cluster.longitude,

                            latitude2 =
                                report.latitude,

                            longitude2 =
                                report.longitude
                        ) <=
                        SAME_CATEGORY_CLUSTER_DISTANCE_METERS
            }

        if (
            existingIndex == -1
        ) {

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
 * BUILD SPIDER GROUPS
 * -------------------------------------------------------------
 *
 * This uses connected groups.
 *
 * So if:
 *
 * A is close to B
 * and B is close to C
 *
 * they are treated as one overlapping map group.
 */

private fun buildSpiderGroups(
    clusters: List<HazardMapCluster>
): List<HazardSpiderGroup> {

    val groups =
        mutableListOf<HazardSpiderGroup>()

    val visited =
        mutableSetOf<Int>()

    for (
    startIndex in clusters.indices
    ) {

        if (
            startIndex in visited
        ) {

            continue
        }

        val queue =
            mutableListOf(
                startIndex
            )

        val memberIndices =
            mutableListOf<Int>()

        visited.add(
            startIndex
        )

        while (
            queue.isNotEmpty()
        ) {

            val currentIndex =
                queue.removeAt(
                    0
                )

            memberIndices.add(
                currentIndex
            )

            val current =
                clusters[
                    currentIndex
                ]

            for (
            candidateIndex in clusters.indices
            ) {

                if (
                    candidateIndex in visited
                ) {

                    continue
                }

                val candidate =
                    clusters[
                        candidateIndex
                    ]

                /*
                 * Same-category clusters are already handled
                 * by the normal 150-meter clustering.
                 */
                if (
                    current.category ==
                    candidate.category
                ) {

                    continue
                }

                val distance =
                    distanceMeters(
                        latitude1 =
                            current.latitude,

                        longitude1 =
                            current.longitude,

                        latitude2 =
                            candidate.latitude,

                        longitude2 =
                            candidate.longitude
                    )

                if (
                    distance <=
                    SPIDER_GROUP_DISTANCE_METERS
                ) {

                    visited.add(
                        candidateIndex
                    )

                    queue.add(
                        candidateIndex
                    )
                }
            }
        }

        /*
         * Only make a spider group if two or more
         * different-category clusters overlap.
         */
        if (
            memberIndices.size > 1
        ) {

            val members =
                memberIndices.map {
                    clusters[
                        it
                    ]
                }

            val centerLatitude =
                members
                    .map {
                        it.latitude
                    }
                    .average()

            val centerLongitude =
                members
                    .map {
                        it.longitude
                    }
                    .average()

            groups.add(
                HazardSpiderGroup(
                    key =
                        members
                            .flatMap {
                                it.reports
                            }
                            .map {
                                it.id
                            }
                            .sorted()
                            .joinToString(
                                separator =
                                    "|"
                            ),

                    latitude =
                        centerLatitude,

                    longitude =
                        centerLongitude,

                    clusters =
                        members
                )
            )
        }
    }

    return groups
}


/*
 * -------------------------------------------------------------
 * CREATE TEMPORARY SPIDER POSITIONS
 * -------------------------------------------------------------
 */

private fun createSpiderMarkers(
    group: HazardSpiderGroup
): List<SpiderMarker> {

    val result =
        mutableListOf<SpiderMarker>()

    group.clusters
        .forEachIndexed { index, cluster ->

            val angle =
                (
                        2.0 *
                                PI *
                                index
                        ) /
                        group.clusters.size

            val northMeters =
                cos(
                    angle
                ) *
                        SPIDER_RADIUS_METERS

            val eastMeters =
                sin(
                    angle
                ) *
                        SPIDER_RADIUS_METERS

            val displayPosition =
                offsetCoordinateByMeters(
                    latitude =
                        group.latitude,

                    longitude =
                        group.longitude,

                    northMeters =
                        northMeters,

                    eastMeters =
                        eastMeters
                )

            result.add(
                SpiderMarker(
                    cluster =
                        cluster,

                    displayLatitude =
                        displayPosition.first,

                    displayLongitude =
                        displayPosition.second
                )
            )
        }

    return result
}


/*
 * -------------------------------------------------------------
 * UNIQUE CLUSTER KEY
 * -------------------------------------------------------------
 */

private fun hazardClusterKey(
    cluster: HazardMapCluster
): String {

    return cluster.reports
        .map {
            it.id
        }
        .sorted()
        .joinToString(
            separator =
                "|"
        )
}


/*
 * -------------------------------------------------------------
 * TEMPORARY COORDINATE OFFSET
 * -------------------------------------------------------------
 *
 * Used only while a spider group is expanded.
 */

private fun offsetCoordinateByMeters(
    latitude: Double,
    longitude: Double,
    northMeters: Double,
    eastMeters: Double
): Pair<Double, Double> {

    val metersPerDegreeLatitude =
        111_320.0

    val latitudeRadians =
        Math.toRadians(
            latitude
        )

    val metersPerDegreeLongitude =
        111_320.0 *
                cos(
                    latitudeRadians
                )

    val latitudeOffset =
        northMeters /
                metersPerDegreeLatitude

    val longitudeOffset =
        if (
            abs(
                metersPerDegreeLongitude
            ) > 0.0001
        ) {

            eastMeters /
                    metersPerDegreeLongitude

        } else {

            0.0
        }

    return Pair(
        latitude +
                latitudeOffset,

        longitude +
                longitudeOffset
    )
}


/*
 * -------------------------------------------------------------
 * REAL DISTANCE BETWEEN LOCATIONS
 * -------------------------------------------------------------
 *
 * Haversine formula.
 */

private fun distanceMeters(
    latitude1: Double,
    longitude1: Double,
    latitude2: Double,
    longitude2: Double
): Double {

    val earthRadiusMeters =
        6_371_000.0

    val latitude1Radians =
        Math.toRadians(
            latitude1
        )

    val latitude2Radians =
        Math.toRadians(
            latitude2
        )

    val latitudeDifference =
        Math.toRadians(
            latitude2 -
                    latitude1
        )

    val longitudeDifference =
        Math.toRadians(
            longitude2 -
                    longitude1
        )

    val a =
        sin(
            latitudeDifference /
                    2.0
        ) *
                sin(
                    latitudeDifference /
                            2.0
                ) +
                cos(
                    latitude1Radians
                ) *
                cos(
                    latitude2Radians
                ) *
                sin(
                    longitudeDifference /
                            2.0
                ) *
                sin(
                    longitudeDifference /
                            2.0
                )

    val c =
        2.0 *
                atan2(
                    sqrt(
                        a
                    ),

                    sqrt(
                        1.0 -
                                a
                    )
                )

    return earthRadiusMeters *
            c
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

        "Accident / Emergency" ->
            Color(
                0xFFD32F2F
            )

        "Wildlife" ->
            Color(
                0xFFF57C00
            )

        "Nature / Plants" ->
            Color(
                0xFF8D6E36
            )

        "Trail / Road Hazard" ->
            Color(
                0xFFFBC02D
            )

        "Weather / Environmental" ->
            Color(
                0xFF1976D2
            )

        "Facility / Infrastructure" ->
            Color(
                0xFF7E57C2
            )

        else ->
            Color(
                0xFF757575
            )
    }
}


/*
 * -------------------------------------------------------------
 * HIGHEST PRIORITY
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