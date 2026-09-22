package com.cos229239.team02.oto.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

//Test the shared distance formatting rules used by OTO.
class DistanceFormatterTest {

    //Check that a short metric distance stays in meters.
    @Test
    fun metricShortDistanceUsesMeters() {

        val result =
            DistanceFormatter.formatDistance(
                meters = 500.0,
                distanceUnit = DistanceUnit.KILOMETERS
            )

        assertEquals(
            "500 m",
            result
        )
    }

    //Check that a longer metric distance switches to kilometers.
    @Test
    fun metricLongDistanceUsesKilometers() {

        val result =
            DistanceFormatter.formatDistance(
                meters = 3218.688,
                distanceUnit = DistanceUnit.KILOMETERS
            )

        assertEquals(
            "3.2 km",
            result
        )
    }

    //Check that a short imperial distance uses feet.
    @Test
    fun imperialShortDistanceUsesFeet() {

        val result =
            DistanceFormatter.formatDistance(
                meters = 500.0,
                distanceUnit = DistanceUnit.MILES
            )

        assertEquals(
            "1640 ft",
            result
        )
    }

    //Check that a longer imperial distance switches to miles.
    @Test
    fun imperialLongDistanceUsesMiles() {

        val result =
            DistanceFormatter.formatDistance(
                meters = 3218.688,
                distanceUnit = DistanceUnit.MILES
            )

        assertEquals(
            "2.0 mi",
            result
        )
    }

    //Check the exact point where metric distance switches to kilometers.
    @Test
    fun oneKilometerSwitchesToKilometers() {

        val result =
            DistanceFormatter.formatDistance(
                meters = 1000.0,
                distanceUnit = DistanceUnit.KILOMETERS
            )

        assertEquals(
            "1.0 km",
            result
        )
    }

    //Check the exact point where imperial distance switches to miles.
    @Test
    fun oneMileSwitchesToMiles() {

        val result =
            DistanceFormatter.formatDistance(
                meters = 1609.344,
                distanceUnit = DistanceUnit.MILES
            )

        assertEquals(
            "1.0 mi",
            result
        )
    }
}