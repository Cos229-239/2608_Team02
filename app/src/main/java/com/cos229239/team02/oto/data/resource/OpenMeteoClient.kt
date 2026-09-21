package com.cos229239.team02.oto.data.resource

import com.cos229239.team02.oto.data.location.OtoLocation
import com.cos229239.team02.oto.data.safety.getSafetyJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.time.Instant
import kotlin.math.roundToInt
import com.cos229239.team02.oto.data.resource.WeatherForecast

class OpenMeteoClient(
    private val httpClient: OkHttpClient

){
    suspend fun getForecast(
        location: OtoLocation
    ): WeatherForecast = withContext(Dispatchers.IO) {
        require(
            location.latitude.isFinite() &&
            location.latitude in -90.0..90.0 &&
            location.longitude.isFinite() &&
            location.longitude in -180.0..180.0
        ){
            "Invalid coordinates"
        }

        val hourlyVariables = listOf(
            "temperature_2m",
            "relative_humidity_2m",
            "apparent_temperature",
            "weather_code",
            "is_day",
            "precipitation_probability",
            "wind_speed_10m",
            "wind_direction_10m"
        )

        val url = "https://api.open-meteo.com/v1/forecast"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter(
                "latitude",
                location.latitude.toString()
            )
            .addQueryParameter(
                "longitude",
                location.longitude.toString()
            )
            .addQueryParameter(
                "hourly",
                hourlyVariables.joinToString(",")
            )
            .addQueryParameter("temperature_unit", "fahrenheit")
            .addQueryParameter("wind_speed_unit", "mph")
            .addQueryParameter("timezone", "auto")
            .addQueryParameter("timeformat", "unixtime")
            .addQueryParameter("forecast_days", "2")
            .build()


        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .build()
        val root = httpClient.getSafetyJson(request)
        val hourly = root.getJSONObject("hourly")
        val times = hourly.getJSONArray("time")
        val now = Instant.now().epochSecond

        val index = (0 until times.length()).firstOrNull{
            times.getLong(it) >= now
        } ?: throw IOException("No upcoming forecast hour returned.")
        fun optionalNumber(name: String): Double? {
            val values = hourly.optJSONArray(name) ?: return null

            if(index >= values.length() || values.isNull(index))
            {
                return null
            }
            return values.optDouble(index)
                .takeIf { it.isFinite() }
        }
        fun requiredNumber(name: String): Double{
            val values = hourly.getJSONArray(name)
            if (values.isNull(index)) {
                throw IOException("Forecast field unavailable: $name")
            }

            val value = values.getDouble(index)

            if (!value.isFinite()) {
                throw IOException("Invalid forecast field: $name")
            }

            return value
        }
        val temperature = requiredNumber("temperature_2m").roundToInt()
        val speed = requiredNumber("wind_speed_10m").roundToInt()
        val direction = requiredNumber("wind_direction_10m")

        val codes = hourly.getJSONArray("weather_code")
        val description = if (codes.isNull(index)) {
            "Conditions unavailable"

    } else {
            describeWeather(codes.getInt(index))
        }
        val probabilities = hourly.optJSONArray("precipitation_probability")
        val probability = if (
            probabilities != null &&
            index < probabilities.length() &&
            !probabilities.isNull(index)
        ) {
            probabilities.getInt(index)
        } else {
            null
        }
        val validTime = times.getLong(index)

        WeatherForecast(
            periodName = "Next hour forecast",
            temp = temperature,
            tempUnit = "F",
            shortForecast = description,
            detailedForecast = "$description. Forecast temperature " +
                    "$temperature°F with wind $speed mph. " +
                    "Precipitation probability applies to the hour " +
                    "ending at the forecast time.",
            windSpeed = "$speed mph",
            windDirection = compassDirection(direction),
            precipitationPercent = probability,
            startTime = Instant.ofEpochSecond(validTime - 3600).toString(),
            endTime = Instant.ofEpochSecond(validTime).toString(),
            humidityPercent = optionalNumber("relative_humidity_2m")
                ?.roundToInt(),
            feelsLike = optionalNumber("apparent_temperature")
                ?.roundToInt(),
            weatherCode = optionalNumber("weather_code")
                ?.toInt(),
            isDay = optionalNumber("is_day")?.let { it == 1.0 }
        )

    }
    suspend fun getAirQuality(
        location: OtoLocation
    ): AirQualityData = withContext(Dispatchers.IO) {
        require(
            location.latitude.isFinite() &&
            location.latitude in -90.0..90.0 &&
            location.longitude.isFinite() &&
            location.longitude in -180.0..180.0
        ){
            "Invalid coordinates"
        }
        val variables = listOf(
            "us_aqi",
            "pm2_5",
            "pm10"
        )
        // Air Quality Api
        val url = "https://air-quality-api.open-meteo.com/v1/air-quality"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("latitude", location.latitude.toString())
            .addQueryParameter("longitude", location.longitude.toString())
            .addQueryParameter("current", "us_aqi,pm2_5,pm10")
            .addQueryParameter("timeformat", "unixtime")
            .build()


        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .build()

        val root = httpClient.getSafetyJson(request)
        val current = root.getJSONObject("current")

        fun optionalValue(name: String): Double? {
            if (!current.has(name) || current.isNull(name)) {
                return null
            }

            return current.optDouble(name)
                .takeIf { it.isFinite() && it >= 0 }
        }
        val aqi = optionalValue("us_aqi")
        val pm25 = optionalValue("pm2_5")
        val pm10 = optionalValue("pm10")

        if (aqi == null && pm25 == null && pm10 == null) {
            throw IOException("Air-quality data unavailable for this location")
        }

        AirQualityData(
            usAqi = aqi?.roundToInt(),
            pm25 = pm25,
            pm10 = pm10,
            validTime = Instant.ofEpochSecond(
                current.getLong("time")
            ).toString()
        )

    }
    private fun compassDirection(degrees: Double): String {
        val directions = listOf(
            "N", "NE", "E", "SE", "S", "SW", "W", "NW"
        )
        return directions[
            (((degrees % 360 + 360) % 360) / 45).roundToInt() % 8
        ]
    }

    private fun describeWeather(code: Int): String = when (code) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing rain"
        71, 73, 75 -> "Snow"
        77 -> "Snow grains"
        80, 81, 82 -> "Rain showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Conditions unavailable"
    }



}