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
    val endTime: String,
    val humidityPercent: Int? = null,
    val feelsLike: Int? = null,
    val weatherCode: Int? = null,
    val isDay: Boolean? = null


)

data class AirQualityData(
    val usAqi: Int?,
    val pm25: Double?,
    val pm10: Double?,
    val validTime: String
){
    val category: String
        get() = when {
            usAqi == null -> "Unavailable"
            usAqi <= 50 -> "Good"
            usAqi <= 100 -> "Moderate"
            usAqi <= 150 -> "Unhealthy for sensitive groups"
            usAqi <= 200 -> "Unhealthy"
            usAqi <= 300 -> "Very Unhealthy"
            else -> "Hazardous"
        }
}