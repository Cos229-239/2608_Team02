package com.cos229239.team02.oto.ui.features

import kotlinx.coroutines.delay

interface AreaSafetyRepo {

    suspend fun getNotified (
        areaName: String
    ): List <SafetyNotification>
}

class DummyAreaSafetyRepo : AreaSafetyRepo {

    override suspend fun getNotified(areaName: String): List<SafetyNotification> {
        delay(1000)

        return listOf(
            SafetyNotification    (
                id = "test weather",
                title = " Test: High Wind Advisory",
                details = """
                    Wind Advisory in effect until [Time] [Time Zone] 
                    Sustained Winds Up to [80 to 100mph] Seek immediate shelter  """.trimIndent(),
                instruct = """
                    Please find the nearest solid structure preferably below 
                    ground for ensured protection from potential hazards continued to last for the next few hours """.trimIndent(),
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

            SafetyNotification(
                id = "test community info",
                title = " Test: Dangerous Animal on Trail",
                details = """
                    User reported Brown Bear located at longitude: 43.5622, -65.0055 at [Time] [Time Zone] 
                    Please beware and cautious """.trimIndent(),
                instruct = """
                    Approach area fully aware or avoid area all together if possible before continuing """.trimIndent(),
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