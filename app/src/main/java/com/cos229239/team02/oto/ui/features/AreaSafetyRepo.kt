package com.cos229239.team02.oto.ui.features

import kotlinx.coroutines.delay

/**
 * Repository used by the Area Safety feature.
 *
 * Right now this uses sample data only.
 * Eric's real NWS / NPS implementation can replace
 * this later without changing the screen UI.
 */
interface AreaSafetyRepo {

    suspend fun getNotified(
        areaName: String
    ): List<SafetyNotification>
}


/**
 * Temporary sample repository.
 *
 * This keeps the existing Area Safety screen working
 * until the real API-backed repository is connected.
 */
class DummyAreaSafetyRepo : AreaSafetyRepo {

    override suspend fun getNotified(
        areaName: String
    ): List<SafetyNotification> {

        /*
         * Simulates a network delay.
         */
        delay(1000)

        return listOf(

            /*
             * -------------------------------------------------
             * SAMPLE WEATHER ALERT
             * -------------------------------------------------
             */
            SafetyNotification(
                id = "test weather",

                title = "Test: High Wind Advisory",

                details = """
                    Wind Advisory in effect until [Time] [Time Zone].
                    Sustained winds up to [80 to 100 mph].
                    Seek immediate shelter.
                """.trimIndent(),

                instruct = """
                    Please find the nearest solid structure,
                    preferably below ground, for protection
                    from potential hazards expected to continue
                    for the next few hours.
                """.trimIndent(),

                affectedArea = areaName,

                category = SafetyCategory.WEATHER,

                level = SafetyLevel.MODERATE,

                sourceID = "weather.com",

                sourceURL = "Weather Channel - TEST",

                retrievedTime = "25AUG26 7:48am",

                lastVerification = "25AUG26 6:42am",

                expires = "25AUG26 10:00am",

                sampleData = true
            ),

            /*
             * -------------------------------------------------
             * SAMPLE COMMUNITY ALERT
             * -------------------------------------------------
             */
            SafetyNotification(
                id = "test community info",

                title = "Test: Dangerous Animal on Trail",

                details = """
                    User reported Brown Bear located at
                    longitude: 43.5622, -65.0055 at
                    [Time] [Time Zone].
                    Please remain cautious.
                """.trimIndent(),

                instruct = """
                    Approach the area fully aware or avoid
                    the area altogether if possible before
                    continuing.
                """.trimIndent(),

                affectedArea = areaName,

                category = SafetyCategory.COMMUNITY,

                level = SafetyLevel.SEVERE,

                sourceID = "https://sample.com",

                sourceURL = "OTO Community Report - TEST",

                retrievedTime = "25AUG26 7:48am",

                lastVerification = "25AUG26 6:42am",

                expires = "25AUG26 10:00am",

                dataExpired = true,

                sampleData = true
            )
        )
    }
}