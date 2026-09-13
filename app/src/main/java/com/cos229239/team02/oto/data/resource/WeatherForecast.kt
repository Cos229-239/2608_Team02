package com.cos229239.team02.oto.data.resource
data class WeatherForecast(
    val periodName: String,
    val temp: Int,
    val tempUnit: String,
    val shortForecast: String,
    val detailedForecast: String,
    val windSpeed: String,
    val windDirection: String,
    val precipitationPercent: Int?,
    val startTime: String,
    val endTime: String


)