package com.cos229239.team02.oto.ui.screens.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cos229239.team02.oto.data.location.AndroidLocationRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException


//Represent the possible location states shown on Home.
sealed interface HomeLocationState {

    data object NotRequested : HomeLocationState

    data object Loading : HomeLocationState

    data class Available(
        val latitude: Double,
        val longitude: Double
    ) : HomeLocationState

    data object PermissionDenied : HomeLocationState

    data object Unavailable : HomeLocationState
}



//Manage Home screen location data outside the Home composable.
class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    //Use the shared Android location repository for device location.
    private val locationRepository =
        AndroidLocationRepository(
            application.applicationContext
        )

    //Hold the current Home location state.
    var locationState by mutableStateOf<HomeLocationState>(
        HomeLocationState.NotRequested
    )
        private set

    //Request the current device location and update the Home state.
    fun refreshLocation() {

        viewModelScope.launch {

            locationState =
                HomeLocationState.Loading

            locationState =
                try {

                    val location =
                        locationRepository.getCurrentLocation()

                    if (location != null) {

                        HomeLocationState.Available(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )

                    } else {

                        HomeLocationState.Unavailable
                    }

                } catch (cancellation: CancellationException) {

                    //Allow normal coroutine cancellation to continue.
                    throw cancellation

                } catch (error: Exception) {

                    //Show an unavailable state if location retrieval fails.
                    HomeLocationState.Unavailable
                }
        }
    }

    //Record that location permission was denied.
    fun locationPermissionDenied() {

        locationState =
            HomeLocationState.PermissionDenied
    }
}