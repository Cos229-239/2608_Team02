package com.cos229239.team02.oto.ui.features

import kotlinx.coroutines.delay

interface AreaSafetyRepo {

    suspend fun getAlerts
}