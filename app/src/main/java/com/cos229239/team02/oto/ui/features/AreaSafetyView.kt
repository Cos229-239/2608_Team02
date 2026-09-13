package com.cos229239.team02.oto.ui.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cos229239.team02.oto.data.location.OtoLocation
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


class AreaSafetyView (
    private val repo: AreaSafetyRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(AreaSafetyUIState())
    val uiState = _uiState.asStateFlow()
    private var selectedLocation: OtoLocation? = null
    private var selectedParkCode: String? = null

    private var allNotifications = emptyList<SafetyNotification>()
    private var requestJob: Job? = null
    private var requestVersion: Long = 0



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
            selectedParkCode = selectedParkCode,
            filterSelected = _uiState.value.filterSelected
        )

        if (selectedLocation != null) {
            loadArea(forceRefresh = false)
        }

    }
    fun selectParkCode(parkCode: String?){
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
                resourceResult = null,
                sources = emptyList(),
                checkedAtMillis = null,
                errorMessage = null
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
                        resourceResult = result.resourceResult,
                        sources = result.sources,
                        checkedAtMillis = result.checkedAtMillis
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
