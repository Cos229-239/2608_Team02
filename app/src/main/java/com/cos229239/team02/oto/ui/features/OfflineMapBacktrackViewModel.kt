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
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

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

    // -----------------------------------------------------------------
    // Backtrack guidance state
    // -----------------------------------------------------------------

    private var trackStartedAtMillis = 0L

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
                    routePoints =
                        routePoints + location.toRoutePoint()
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

        trackStartedAtMillis =
            System.currentTimeMillis()

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
                    id = UUID.randomUUID().toString(),
                    startedAtMillis = trackStartedAtMillis,
                    endedAtMillis = System.currentTimeMillis(),
                    points = routePoints
                )

            trackingStatus =
                "Route saved (${routePoints.size} points)"

            viewModelScope.launch {

                if (!routeStorage.save(route)) {
                    saveRouteError =
                        "Could not save your route"
                }
            }

        } else {

            trackingStatus =
                "Not enough points recorded to save a route"
        }
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
            ) <= WAYPOINT_REACHED_METERS
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
        // the reached threshold, a target always exists here.
        val reversed = points.asReversed()

        var target: RoutePoint? = null

        for (point in reversed) {

            val distance =
                distanceMeters(
                    current.latitude,
                    current.longitude,
                    point.latitude,
                    point.longitude
                )

            if (distance > WAYPOINT_REACHED_METERS) {
                target = point
                break
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