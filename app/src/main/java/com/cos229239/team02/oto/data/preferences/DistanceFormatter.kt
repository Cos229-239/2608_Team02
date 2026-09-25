package com.cos229239.team02.oto.data.preferences

import java.util.Locale
import kotlin.math.roundToInt

//Format OTO distance values using the user's selected unit system.
object DistanceFormatter {

    private const val METERS_PER_MILE =
        1609.344

    private const val FEET_PER_METER =
        3.28084

    //Format a distance stored internally in meters.
    fun formatDistance(
        meters: Double,
        distanceUnit: DistanceUnit
    ): String {

        return when (distanceUnit) {

            DistanceUnit.MILES ->
                formatImperialDistance(
                    meters
                )

            DistanceUnit.KILOMETERS ->
                formatMetricDistance(
                    meters
                )
        }
    }

    //Use feet for short imperial distances and miles for longer distances.
    private fun formatImperialDistance(
        meters: Double
    ): String {

        return if (meters >= METERS_PER_MILE) {

            String.format(
                Locale.US,
                "%.1f mi",
                meters / METERS_PER_MILE
            )

        } else {

            val feet =
                (meters * FEET_PER_METER)
                    .roundToInt()

            "$feet ft"
        }
    }

    //Use meters for short metric distances and kilometers for longer distances.
    private fun formatMetricDistance(
        meters: Double
    ): String {

        return if (meters >= 1000.0) {

            String.format(
                Locale.US,
                "%.1f km",
                meters / 1000.0
            )

        } else {

            "${meters.roundToInt()} m"
        }
    }
}