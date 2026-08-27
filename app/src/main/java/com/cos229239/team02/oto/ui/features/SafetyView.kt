package com.cos229239.team02.oto.ui.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SafetyView : ViewModel() {
    private val repo: AreaSafetyRepo = DummyAreaSafetyRepo()

    private val uiState = MutableStateFlow(
        AreaSafetyUIState()

    )

    val uiState: StateFlow<AreaSafetyUIState> = uiState.asStateFlow()

    private
}