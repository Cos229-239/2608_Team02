package com.cos229239.team02.oto.data.safety

import com.cos229239.team02.oto.data.location.OtoLocation

interface AreaSafetyRepo {

    suspend fun getAreaSafety (
        location: OtoLocation,
        areaName: String,
        parkCode: String? = null,
        forceRefresh: Boolean = false
    ): AreaSafetyData
}