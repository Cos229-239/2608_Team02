package com.cos229239.team02.oto.ui.features

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cos229239.team02.oto.data.location.AndroidLocationRepository
import com.cos229239.team02.oto.data.location.OtoLocation
import com.cos229239.team02.oto.data.route.RoutePoint
import com.cos229239.team02.oto.data.route.RouteStorage
import com.cos229239.team02.oto.data.route.RouteTracker
import com.cos229239.team02.oto.data.route.TrackedRoute
import com.cos229239.team02.oto.data.route.toOtoLocation
import com.cos229239.team02.oto.data.route.toRoutePoint
import com.cos229239.team02.oto.ui.components.map.OTO_MAP_STYLE_URL
import kotlinx.coroutines.launch
import org.maplibre.compose.offline.OfflineManager
import org.maplibre.compose.offline.OfflinePack
import org.maplibre.compose.offline.OfflinePackDefinition
import org.maplibre.compose.offline.getOfflineManager
import org.maplibre.spatialk.geojson.BoundingBox
import java.util.Locale
import java.util.UUID
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Holds route tracking, backtrack guidance and offline map state for
 * the Offline Maps & Backtrack screen.
 */
class OfflineMapBacktrackViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val routeTracker =
        RouteTracker(application)

    private val locationRepository =
        AndroidLocationRepository(application)

    private val routeStorage =
        RouteStorage(application)

    private val offlineManager: OfflineManager =
        getOfflineManager(application)

    // -----------------------------------------------------------------
    // Location & Tracking state
    // -----------------------------------------------------------------

    var currentLocation by mutableStateOf<OtoLocation?>(null)
        private set

    var isTracking by mutableStateOf(false)
        private set

    var isBacktracking by mutableStateOf(false)
        private set

    var routePoints by mutableStateOf<List<RoutePoint>>(emptyList())
        private set

    var locationStatus by mutableStateOf(LOCATION_NOT_LOADED)
        private set

    var trackingStatus by mutableStateOf("Not tracking")
        private set

    var backtrackStatus by mutableStateOf("Backtrack not active")
        private set

    var saveRouteError by mutableStateOf<String?>(null)
        private set

    var previousSessionRouteLoaded by mutableStateOf(false)
        private set

    var routeSummary by mutableStateOf<String?>(null)
        private set

    // -----------------------------------------------------------------
    // Backtrack guidance state
    // -----------------------------------------------------------------

    private var trackStartedAtMillis = 0L

    private var currentRouteId: String? = null

    private var lastAutoSaveAtMillis = 0L

    private var lastAutoSavePointCount = 0

    private var lastGuideIndex = 0

    var guideTarget by mutableStateOf<OtoLocation?>(null)
        private set

    var guideDistanceMeters by mutableStateOf<Double?>(null)
        private set

    var guideBearingDegrees by mutableStateOf<Double?>(null)
        private set

    // -----------------------------------------------------------------
    // Offline map state
    // -----------------------------------------------------------------

    var downloadError by mutableStateOf<String?>(null)
        private set

    /** Backed by Compose state; reads in composition track download changes. */
    val offlinePacks: Set<OfflinePack>
        get() = offlineManager.packs

    init {
        viewModelScope.launch {
            routeTracker.locations.collect { location ->

                currentLocation = location
                locationStatus = "Current location found"

                if (isTracking) {

                    val lastPoint =
                        routePoints.lastOrNull()

                    val farEnoughFromLastPoint =
                        lastPoint == null ||
                            distanceMeters(
                                lastPoint.latitude,
                                lastPoint.longitude,
                                location.latitude,
                                location.longitude
                            ) >= MIN_POINT_SPACING_METERS

                    val accurateEnough =
                        (location.accuracyMeters ?: 0f) <=
                            MAX_ACCURACY_METERS

                    if (farEnoughFromLastPoint && accurateEnough) {
                        routePoints =
                            routePoints + location.toRoutePoint()
                    }

                    autoSaveIfDue()
                }

                if (isBacktracking) {
                    updateBacktrackGuide()
                }
            }
        }

        viewModelScope.launch {
            val saved = routeStorage.loadLatest()

            if (
                saved != null &&
                saved.points.size >= MIN_ROUTE_POINTS
            ) {
                routePoints = saved.points
                previousSessionRouteLoaded = true
                trackingStatus =
                    "Loaded route from last session " +
                        "(${saved.points.size} points)"
            }
        }
    }

    override fun onCleared() {
        routeTracker.stop()
    }

    /**
     * Records that location permission was denied by the user.
     */
    fun locationPermissionDenied() {
        locationStatus = "Location permission denied"
    }

    /**
     * Keeps GPS updates flowing while tracking OR backtracking, and
     * stops them when neither is active. This lets backtrack guidance
     * follow the user as they walk without recording new route points.
     */
    private fun updateTrackerState() {

        if (isTracking || isBacktracking) {

            if (!routeTracker.isActive) {
                routeTracker.start()
            }

        } else {

            if (routeTracker.isActive) {
                routeTracker.stop()
            }
        }
    }

    // -----------------------------------------------------------------
    // Route tracking
    // -----------------------------------------------------------------

    /**
     * Gets one fresh GPS fix, for example after the user grants
     * location permission.
     */
    fun refreshCurrentLocation() {

        viewModelScope.launch {

            val location =
                locationRepository.getCurrentLocation()

            currentLocation = location

            locationStatus =
                if (location != null) {
                    "Current location found"
                } else {
                    "Unable to determine current location"
                }

            if (isBacktracking) {
                updateBacktrackGuide()
            }
        }
    }

    /**
     * Starts recording a new route. Any previously shown route is cleared.
     */
    fun startTracking() {

        if (isTracking) {
            return
        }

        isTracking = true
        isBacktracking = false
        routePoints = emptyList()
        previousSessionRouteLoaded = false
        routeSummary = null
        lastGuideIndex = 0

        currentRouteId =
            UUID.randomUUID().toString()

        trackStartedAtMillis =
            System.currentTimeMillis()

        lastAutoSaveAtMillis =
            System.currentTimeMillis()

        lastAutoSavePointCount = 0

        trackingStatus =
            "Tracking route... " +
                "please grant location permission if asked."

        backtrackStatus =
            "Backtrack not active"

        guideTarget = null
        guideDistanceMeters = null
        guideBearingDegrees = null

        updateTrackerState()
    }

    /**
     * Stops recording and saves the route for the next app session.
     */
    fun stopTracking() {

        if (!isTracking) {
            return
        }

        isTracking = false
        updateTrackerState()

        if (routePoints.size >= MIN_ROUTE_POINTS) {

            val route =
                TrackedRoute(
                    id = currentRouteId
                        ?: UUID.randomUUID().toString(),
                    startedAtMillis = trackStartedAtMillis,
                    endedAtMillis = System.currentTimeMillis(),
                    points = routePoints
                )

            routeSummary =
                buildRouteSummary(route)

            val saved =
                routeStorage.saveBlocking(route)

            trackingStatus =
                if (saved) {
                    "Route saved (${routePoints.size} points)"
                } else {
                    "Could not save your route"
                }

        } else {

            trackingStatus =
                "Not enough points recorded to save a route"

            routeSummary = null
        }

        currentRouteId = null
    }

    /**
     * Persists the current route so a killed app still has the session
     * up to the last checkpoint. Saves whenever the route has grown by
     * enough recorded points, or after a fixed time has passed.
     */
    private fun autoSaveIfDue() {

        val now = System.currentTimeMillis()
        val routeId = currentRouteId ?: return

        val gainedEnoughPoints =
            routePoints.size - lastAutoSavePointCount >=
            AUTO_SAVE_POINT_TRIGGER

        if (
            isTracking &&
            (now - lastAutoSaveAtMillis >= AUTO_SAVE_INTERVAL_MILLIS ||
                gainedEnoughPoints)
        ) {
            lastAutoSaveAtMillis = now
            lastAutoSavePointCount = routePoints.size

if (routePoints.size >= MIN_ROUTE_POINTS) {

                val checkpoint =
                    TrackedRoute(
                        id = routeId,
                        startedAtMillis = trackStartedAtMillis,
                        endedAtMillis = now,
                        points = routePoints
                    )

                val saved =
                    routeStorage.saveBlocking(checkpoint)

                trackingStatus =
                    if (saved) {
                        "Checkpoint saved (${routePoints.size} points)"
                    } else {
                        "Checkpoint failed to save"
                    }
            }
        }
    }

    /**
     * Persists the current route immediately, for example when the app
     * moves to the background, so a later app kill still has the whole
     * session. Saves synchronously so the write reaches disk before
     * the process can be killed.
     */
    fun saveNowIfTracking() {

        val routeId = currentRouteId ?: return

        if (
            isTracking &&
            routePoints.size >= MIN_ROUTE_POINTS
        ) {

            val checkpoint =
                TrackedRoute(
                    id = routeId,
                    startedAtMillis = trackStartedAtMillis,
                    endedAtMillis = System.currentTimeMillis(),
                    points = routePoints
                )

            routeStorage.saveBlocking(checkpoint)
        }
    }

    /**
     * Reloads the most recently saved route, for example after a new
     * tracking session replaced the one currently shown.
     */
    fun loadLastSavedRoute() {

        viewModelScope.launch {

            val saved = routeStorage.loadLatest()

            if (
                saved != null &&
                saved.points.size >= MIN_ROUTE_POINTS
            ) {
                routePoints = saved.points
                previousSessionRouteLoaded = true
                trackingStatus =
                    "Loaded route from last session " +
                        "(${saved.points.size} points)"
            } else {
                trackingStatus =
                    if (routeStorage.latestRouteExists()) {
                        "Saved route could not be read"
                    } else {
                        "No saved route to load"
                    }
            }
        }
    }

    /**
     * Deletes the previously saved route so a new one can be recorded.
     */
    fun clearPreviousRoute() {

        routeStorage.clearSavedRoutes()

        routePoints = emptyList()
        previousSessionRouteLoaded = false
        routeSummary = null
        lastGuideIndex = 0

        guideTarget = null
        guideDistanceMeters = null
        guideBearingDegrees = null

        trackingStatus =
            "No previous route"
        backtrackStatus =
            "Backtrack not active"

        updateTrackerState()
    }

    /**
     * Summarises a saved route: distance walked, duration and speed.
     */
    private fun buildRouteSummary(
        route: TrackedRoute
    ): String {

        val points = route.points

        var totalDistanceMeters = 0.0

        for (index in 1 until points.size) {
            totalDistanceMeters +=
                distanceMeters(
                    points[index - 1].latitude,
                    points[index - 1].longitude,
                    points[index].latitude,
                    points[index].longitude
                )
        }

        val durationSeconds =
            (((route.endedAtMillis ?: route.startedAtMillis) -
                route.startedAtMillis)
                .toDouble() / 1000.0)
                .coerceAtLeast(0.0)

        val averageSpeedKmH =
            if (durationSeconds > 0.0) {
                totalDistanceMeters / durationSeconds * 3.6
            } else {
                0.0
            }

        return "Walked ${formatDistance(totalDistanceMeters)} · " +
            "${formatDuration(durationSeconds)} · " +
            "avg ${"%.1f".format(Locale.US, averageSpeedKmH)} km/h"
    }

    // -----------------------------------------------------------------
    // Backtrack guidance
    // -----------------------------------------------------------------

    /**
     * Begins guiding the user back along the current (or saved) route.
     */
    fun startBacktrack() {

        val points = routePoints

        if (points.size < MIN_ROUTE_POINTS) {

            backtrackStatus =
                "Track a route first " +
                    "(need at least 2 points)"

            return
        }

        isBacktracking = true

        lastGuideIndex = 0

        backtrackStatus =
            "Guiding you back along your route"

        updateTrackerState()
        updateBacktrackGuide()
    }

    /**
     * Stops backtracking guidance.
     */
    fun stopBacktrack() {

        isBacktracking = false

        backtrackStatus =
            "Backtrack not active"

        guideTarget = null
        guideDistanceMeters = null
        guideBearingDegrees = null

        updateTrackerState()
    }

    /**
     * Walks the reversed route and picks the nearest waypoint that the
     * user has not reached yet, then reports its distance and bearing.
     */
    private fun updateBacktrackGuide() {

        val current = currentLocation
        val points = routePoints

        if (
            current == null ||
            points.size < MIN_ROUTE_POINTS
        ) {
            return
        }

        // The reached threshold grows with GPS uncertainty so a poor
        // fix never declares a waypoint reached too early.
        val reachThreshold =
            max(
                WAYPOINT_REACHED_METERS,
                ((current.accuracyMeters ?: 0f) *
                    ACCURACY_THRESHOLD_MULTIPLIER).toDouble()
            )

        // The original start of the route (the end of the reversed path).
        val routeStart = points.first()

        // Only treat the user as arrived when they are actually close
        // to the route's starting point.
        if (
            distanceMeters(
                current.latitude,
                current.longitude,
                routeStart.latitude,
                routeStart.longitude
            ) <= reachThreshold
        ) {

            backtrackStatus =
                "You are back at the start of your route"

            guideTarget = null
            guideDistanceMeters = null
            guideBearingDegrees = null

            return
        }

        // Pick the first waypoint (walking backward) that the user
        // has not reached yet. Since the start point is farther than
        // the reached threshold, a target always exists here. The scan
        // resumes from the last chosen waypoint so each GPS fix does
        // not re-walk the whole route.
        val reversed = points.asReversed()

        var target: RoutePoint? = null

        val startIndex =
            lastGuideIndex.coerceIn(0, reversed.lastIndex)

        for (index in startIndex until reversed.size) {

            val point = reversed[index]

            val distance =
                distanceMeters(
                    current.latitude,
                    current.longitude,
                    point.latitude,
                    point.longitude
                )

            if (distance > reachThreshold) {
                target = point
                lastGuideIndex = index
                break
            }
        }

        // If the cursor found nothing ahead, the user may have wandered
        // sideways; fall back to scanning the waypoints behind it.
        if (target == null) {

            for (index in 0 until startIndex) {

                val point = reversed[index]

                val distance =
                    distanceMeters(
                        current.latitude,
                        current.longitude,
                        point.latitude,
                        point.longitude
                    )

                if (distance > reachThreshold) {
                    target = point
                    lastGuideIndex = index
                    break
                }
            }
        }

        val nextWaypoint = target ?: routeStart

        guideTarget =
            nextWaypoint.toOtoLocation()

        guideDistanceMeters =
            distanceMeters(
                current.latitude,
                current.longitude,
                nextWaypoint.latitude,
                nextWaypoint.longitude
            )

        guideBearingDegrees =
            bearingDegrees(
                current.latitude,
                current.longitude,
                nextWaypoint.latitude,
                nextWaypoint.longitude
            )
    }

    /**
     * Human-readable backtrack instruction, e.g.
     * "Head NE (45°) for 210 m".
     */
    val backtrackGuidance: String
        get() {

            val distance = guideDistanceMeters
            val bearing = guideBearingDegrees

            if (distance == null || bearing == null) {
                return "Waiting for your location..."
            }

            return "Head ${cardinalDirection(bearing)} " +
                "(${round(bearing).toInt()}°) for " +
                formatDistance(distance)
        }

    // -----------------------------------------------------------------
    // Offline map downloads
    // -----------------------------------------------------------------

    /**
     * Downloads an offline region centered on the current location.
     */
    fun downloadRegion(
        option: OfflineRegionOption
    ) {

        val location = currentLocation

        if (location == null) {

            downloadError =
                "Your location is needed to choose a download area"

            return
        }

        if (
            offlinePacks.any { pack ->
                regionNameFor(pack) == option.name
            }
        ) {
            return
        }

        viewModelScope.launch {

            runCatching {

                val definition =
                    OfflinePackDefinition.TilePyramid(
                        styleUrl = OTO_MAP_STYLE_URL,
                        bounds = BoundingBox(
                            west =
                                location.longitude -
                                    option.radiusDegrees,
                            south =
                                location.latitude -
                                    option.radiusDegrees,
                            east =
                                location.longitude +
                                    option.radiusDegrees,
                            north =
                                location.latitude +
                                    option.radiusDegrees
                        ),
                        minZoom = option.minZoom,
                        maxZoom = option.maxZoom
                    )

                val pack =
                    offlineManager.create(
                        definition = definition,
                        metadata =
                            option.name.encodeToByteArray()
                    )

                // Packs start paused; resume starts the download.
                offlineManager.resume(pack)

                downloadError = null

            }.onFailure { error ->

                downloadError =
                    "Download failed: " +
                        (error.message
                            ?: error.javaClass.simpleName)
            }
        }
    }

    /**
     * The region name stored as the pack's metadata, if any.
     */
    fun regionNameFor(
        pack: OfflinePack
    ): String? =
        pack.metadata?.decodeToString()

    /**
     * Deletes a previously downloaded region.
     */
    fun deleteRegion(
        pack: OfflinePack
    ) {

        viewModelScope.launch {

            runCatching {

                offlineManager.delete(pack)

                downloadError = null

            }.onFailure { error ->

                downloadError =
                    "Could not delete map: " +
                        (error.message
                            ?: error.javaClass.simpleName)
            }
        }
    }

    fun clearDownloadError() {
        downloadError = null
    }

    // -----------------------------------------------------------------
    // Geographic helpers
    // -----------------------------------------------------------------

    companion object {

        const val MIN_ROUTE_POINTS = 2

        private const val LOCATION_NOT_LOADED =
            "Location not loaded"

        private const val WAYPOINT_REACHED_METERS =
            25.0

        /** How far a waypoint must be before the user counts as passed. */
        private const val ACCURACY_THRESHOLD_MULTIPLIER =
            2f

        /** Route points closer than this are not recorded. */
        const val MIN_POINT_SPACING_METERS =
            5.0

        /** GPS fixes less accurate than this are not recorded. */
        const val MAX_ACCURACY_METERS =
            50f

        private const val AUTO_SAVE_INTERVAL_MILLIS =
            60_000L

        /** How many recorded points trigger another checkpoint save. */
        private const val AUTO_SAVE_POINT_TRIGGER =
            6

        /** Above this many estimated tiles a region shows a warning. */
        const val TILE_WARNING_LIMIT =
            6_000L

        private const val EARTH_RADIUS_METERS =
            6_371_000.0

        val DEFAULT_REGION_OPTIONS =
            listOf(
                OfflineRegionOption(
                    name = "Current Area",
                    description =
                        "Everything within walking distance " +
                            "of where you are now.",
                    radiusDegrees = 0.008,
                    minZoom = 13,
                    maxZoom = 15
                ),
                OfflineRegionOption(
                    name = "Nearby Trails",
                    description =
                        "A few kilometres of surrounding trails.",
                    radiusDegrees = 0.025,
                    minZoom = 10,
                    maxZoom = 15
                ),
                OfflineRegionOption(
                    name = "Full Region",
                    description =
                        "A wide area around your location.",
                    radiusDegrees = 0.08,
                    minZoom = 5,
                    maxZoom = 15
                )
            )

        private fun distanceMeters(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double
        ): Double {

            val latDelta =
                Math.toRadians(lat2 - lat1)
            val lonDelta =
                Math.toRadians(lon2 - lon1)

            val a =
                sin(latDelta / 2) * sin(latDelta / 2) +
                    cos(Math.toRadians(lat1)) *
                    cos(Math.toRadians(lat2)) *
                    sin(lonDelta / 2) * sin(lonDelta / 2)

            return 2 * EARTH_RADIUS_METERS *
                asin(sqrt(a))
        }

        private fun bearingDegrees(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double
        ): Double {

            val lonDelta =
                Math.toRadians(lon2 - lon1)
            val lat1Radians =
                Math.toRadians(lat1)
            val lat2Radians =
                Math.toRadians(lat2)

            val y =
                sin(lonDelta) * cos(lat2Radians)
            val x =
                cos(lat1Radians) * sin(lat2Radians) -
                    sin(lat1Radians) * cos(lat2Radians) *
                    cos(lonDelta)

            return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
        }

        private fun cardinalDirection(
            bearingDegrees: Double
        ): String {

            val index =
                ((bearingDegrees + 22.5) / 45.0)
                    .toInt() % 8

            return CARDINAL_DIRECTIONS[index]
        }

        private val CARDINAL_DIRECTIONS =
            listOf(
                "N", "NE", "E", "SE",
                "S", "SW", "W", "NW"
            )

        private fun formatDistance(
            meters: Double
        ): String =

            if (meters >= 1000.0) {

                String.format(
                    Locale.US,
                    "%.1f km",
                    meters / 1000.0
                )

            } else {

                "${round(meters).toInt()} m"
            }

        private fun formatDuration(
            totalSeconds: Double
        ): String {

            val seconds = round(totalSeconds).toLong()
            val hours = seconds / 3_600
            val minutes = (seconds % 3_600) / 60
            val remainingSeconds = seconds % 60

            return when {
                hours > 0L -> "${hours}h ${minutes}m"
                minutes > 0L -> "${minutes}m ${remainingSeconds}s"
                else -> "${remainingSeconds}s"
            }
        }

        /**
         * Estimates how many tiles a TilePyramid region needs, using the
         * same degree offsets the [downloadRegion] bounds use. Longitude
         * span is latitude-independent; latitude uses inverse Mercator.
         */
        fun estimatedTilesFor(
            option: OfflineRegionOption,
            latitude: Double
        ): Long {

            var totalTiles = 0L

            for (zoom in option.minZoom..option.maxZoom) {

                val tilesPerWorld =
                    1L shl zoom

                val lonTiles =
                    ceil(
                        tilesPerWorld *
                            2 *
                            option.radiusDegrees /
                            360.0
                    )

                val yNorth =
                    mercatorYTile(
                        latitude + option.radiusDegrees,
                        tilesPerWorld
                    )

                val ySouth =
                    mercatorYTile(
                        latitude - option.radiusDegrees,
                        tilesPerWorld
                    )

                val latTiles =
                    abs(yNorth - ySouth).coerceAtLeast(1.0)

                totalTiles +=
                    (lonTiles * latTiles)
                        .toLong()
                        .coerceAtLeast(1L)
            }

            return totalTiles
        }

        private fun mercatorYTile(
            latitudeDegrees: Double,
            tilesPerWorld: Long
        ): Double {

            val phi =
                Math.toRadians(latitudeDegrees)

            return (
                1.0 -
                    ln(
                        tan(phi) +
                            1.0 / cos(phi)
                    ) /
                    PI
            ) / 2.0 * tilesPerWorld
        }
    }
}

/**
 * A downloadable offline map region option shown in the UI.
 */
data class OfflineRegionOption(
    val name: String,
    val description: String,
    val radiusDegrees: Double,
    val minZoom: Int,
    val maxZoom: Int
)