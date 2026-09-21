package com.cos229239.team02.oto.ui.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cos229239.team02.oto.data.location.OtoLocation
import com.cos229239.team02.oto.data.resource.NpsAlertClient
import com.cos229239.team02.oto.data.safety.AreaSafetyRepo
import com.cos229239.team02.oto.data.safety.SafetyNotification
import com.cos229239.team02.oto.data.safety.SafetyCategory
import com.cos229239.team02.oto.data.safety.SafetyFilter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import android.location.Location
import com.cos229239.team02.oto.data.safety.NpsParkOption



class AreaSafetyView (
    private val repo: AreaSafetyRepo,
    private val npsAlertClient: NpsAlertClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(AreaSafetyUIState())
    val uiState = _uiState.asStateFlow()
    private var selectedLocation: OtoLocation? = null
    private var selectedParkCode: String? = null

    private var allNotifications = emptyList<SafetyNotification>()
    private var requestJob: Job? = null
    private var requestVersion: Long = 0

    private var nearestParkJob: Job? = null
    private var cachedParks: List<NpsParkOption>? = null

    private fun findNearestPark(location: OtoLocation)
    {
        nearestParkJob?.cancel()

        nearestParkJob = viewModelScope.launch {
            try {
                val parks = cachedParks
                    ?: npsAlertClient.getParkOptions().also {
                        cachedParks = it
                    }
                val nearest = parks.mapNotNull { park ->
                    val latitude = park.latitude
                        ?: return@mapNotNull null
                    val longitude = park.longitude
                        ?: return@mapNotNull null

                    if (
                        !latitude.isFinite() ||
                        latitude !in -90.0..90.0 ||
                        !longitude.isFinite() ||
                        longitude !in -180.0..180.0
                    ){
                        return@mapNotNull null
                    }
                    val distance = FloatArray(1)

                    Location.distanceBetween(
                        location.latitude,
                        location.longitude,
                        latitude,
                        longitude,
                        distance
                    )

                    park to distance[0]
                }.minByOrNull { it.second }?.first

                if (nearest != null) {
                    applyNearestPark(
                        parkCode = nearest.parkCode,
                        location = location
                    )

                    _uiState.update { state ->
                        if (state.selectedParkCode == nearest.parkCode) {
                            state.copy(selectedParkName = nearest.fullName)
                        } else {
                            state
                        }
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                android.util.Log.e(
                    "OTO_NEAREST_PARK",
                    "Unable to select nearest park",
                    error
                )
            }
        }
    }


    fun setArea(
        location: OtoLocation?,
        areaName: String,
        parkCode: String? = selectedParkCode
    ) {
        val normalizedParkCode = parkCode
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotEmpty() }
        val previousLocation = selectedLocation

        val sameCoordinates =
            previousLocation?.latitude == location?.latitude &&
                    previousLocation?.longitude == location?.longitude

        if (
            sameCoordinates &&
            _uiState.value.areaName == areaName &&
            selectedParkCode == normalizedParkCode
        ) {
            return
        }
        if (!sameCoordinates){
            nearestParkJob?.cancel()
        }

        requestVersion++
        requestJob?.cancel()

        selectedLocation = if (location != null) {
            OtoLocation(
                latitude = location.latitude,
                longitude = location.longitude
            )
        } else {
            null
        }

        selectedParkCode = normalizedParkCode
        allNotifications = emptyList()

        _uiState.value = AreaSafetyUIState(
            areaName = areaName,
            hasLocation = selectedLocation != null,
            selectedLocation = selectedLocation,
            selectedParkCode = selectedParkCode,
            autoParkSelect = _uiState.value.autoParkSelect,
            filterSelected = _uiState.value.filterSelected
        )

        if (selectedLocation != null) {
            loadArea(forceRefresh = false)
        }

        val locationOfPark = selectedLocation

        if (
            !sameCoordinates &&
            locationOfPark != null &&
            _uiState.value.autoParkSelect
        ){
            findNearestPark(locationOfPark)
        }


    }
    fun selectParkCode(parkCode: String?){
        _uiState.update {
            it.copy(autoParkSelect = false)
        }
        setArea(
            location = selectedLocation,
            areaName = _uiState.value.areaName,
            parkCode = parkCode
        )
    }

    fun useNearestPark() {
        _uiState.update {
            it.copy(autoParkSelect = true)
        }

        selectedLocation?.let { location ->
            findNearestPark(location)
        }
    }
    //Ignore an auto result if user changed more or location.
    fun applyNearestPark(
        parkCode: String,
        location: OtoLocation
    ){
        if (!_uiState.value.autoParkSelect) return

        if (
            selectedLocation?.latitude != location.latitude ||
            selectedLocation?.longitude != location.longitude
        ){
            return
        }
        setArea(
            location = selectedLocation,
            areaName = _uiState.value.areaName,
            parkCode = parkCode
        )
    }
//Loads safety updates according to OTO Location/Area
    fun refreshNotifications() {
        loadArea(forceRefresh = true)
    }

    private fun loadArea(
        forceRefresh: Boolean = false
    ) {
        val location = selectedLocation ?: return

        requestJob?.cancel()

        val version = ++requestVersion
        val areaName = _uiState.value.areaName
        val parkCode = selectedParkCode

        allNotifications = emptyList()

        _uiState.update {
            it.copy(
                isLoading = true,
                notifications = emptyList(),
                weatherNotifications = emptyList(),
                forecast = null,
                airQuality = null,
                resourceResult = null,
                sources = emptyList(),
                checkedAtMillis = null,
                errorMessage = null,


            )
        }

        requestJob = viewModelScope.launch {
            try {
                val result = repo.getAreaSafety(
                    location = location,
                    areaName = areaName,
                    parkCode = parkCode,
                    forceRefresh = forceRefresh
                )
                if (version != requestVersion) {
                    return@launch
                }

                allNotifications = result.notifications

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        notifications = filterNotifications(state.filterSelected),
                        weatherNotifications = allNotifications.filter {
                            it.category == SafetyCategory.WEATHER

                        },
                        forecast = result.forecast,
                        airQuality = result.airQuality,
                        resourceResult = result.resourceResult,
                        sources = result.sources,
                        checkedAtMillis = result.checkedAtMillis,

                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (version == requestVersion) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Unable to update Area Safety. Please retry."
                        )
                    }
                }
            }
        }
    }


//Updates visible notifications using results already loaded
    fun selectFilter(
        filter: SafetyFilter
    ) {
        _uiState.update {
            it.copy(
                filterSelected = filter,
                notifications = filterNotifications(filter)
            )
        }
    }

    private fun filterNotifications(
        filter: SafetyFilter
    ): List<SafetyNotification> = allNotifications.filter {
        when (filter) {
            SafetyFilter.ALL -> true
            SafetyFilter.WEATHER ->
                it.category == SafetyCategory.WEATHER
            SafetyFilter.AREA ->
                it.category == SafetyCategory.AREA
            SafetyFilter.COMMUNITY ->
                it.category == SafetyCategory.COMMUNITY
        }
    }
}
