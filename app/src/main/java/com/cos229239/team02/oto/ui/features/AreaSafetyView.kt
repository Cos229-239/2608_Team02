package com.cos229239.team02.oto.ui.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AreaSafetyView : ViewModel() {
    private val repo: AreaSafetyRepo = DummyAreaSafetyRepo()

    private val _uiState = MutableStateFlow(
        AreaSafetyUIState()

    )

    val uiState: StateFlow<AreaSafetyUIState> = _uiState.asStateFlow()

    private var allNotifications: List<SafetyNotification> = emptyList()

    init {
        refreshNotifications()

    }

    fun refreshNotifications() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val notifications = repo.getNotified(areaName = _uiState.value.areaName)

                allNotifications = notifications

                _uiState.update { currentState ->
                    currentState.copy(
                        notifications = filterNotifications(
                            notifications = notifications,
                            filter = currentState.filterSelected
                        ),
                        isLoading = false,
                        errorMessage = null,
                        isSampleData = notifications.any {
                            it.sampleData
                        }
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = error.message
                            ?: "Unable to load safety notifications."
                    )
                }
            }
        }
    }

    private fun filterNotifications(notifications: List<SafetyNotification>,
                                    filter: SafetyFilter
    ): List<SafetyNotification> {
        return when (filter) {
        SafetyFilter.ALL -> notifications
            SafetyFilter.WEATHER -> { notifications.filter { notification ->
                notification.category == SafetyCategory.WEATHER
              }
            }
            SafetyFilter.AREA -> { notifications.filter{notification ->
                notification.category == SafetyCategory.AREA
            }
            }
            SafetyFilter.COMMUNITY -> { notifications.filter { notification ->
                notification.category == SafetyCategory.COMMUNITY
                }

            }
        }
    }

    fun selectFilter(filter: SafetyFilter) {
        _uiState.update { currentState ->
            currentState.copy(
                filterSelected = filter,
                notifications = filterNotifications(
                   notifications = allNotifications,
                    filter = filter
               )
           )

        }
    }
}