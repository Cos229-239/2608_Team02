package com.cos229239.team02.oto.data.hazard

/*
 * -------------------------------------------------------------
 * HAZARD PRIORITY
 * -------------------------------------------------------------
 *
 * Priority is assigned automatically by OTO.
 */
enum class HazardPriority {
    NORMAL,
    HIGH,
    CRITICAL
}


/*
 * -------------------------------------------------------------
 * HAZARD REPORT
 * -------------------------------------------------------------
 *
 * Represents one submitted hazard report.
 *
 * Reports are currently saved locally so they can survive
 * app restarts.
 *
 * A future backend will allow these reports and confirmations
 * to be shared between different OTO users.
 */
data class HazardReport(

    /*
     * Unique report identifier.
     */
    val id: String,

    /*
     * Broad report category.
     *
     * Example:
     * Wildlife
     */
    val category: String,

    /*
     * Specific hazard type.
     *
     * Example:
     * Bear Sighting
     */
    val reportType: String,

    /*
     * User-selected severity.
     *
     * Low
     * Moderate
     * High
     */
    val severity: String,

    /*
     * Priority calculated automatically.
     */
    val priority: HazardPriority,

    /*
     * GPS location where the report was created.
     */
    val latitude: Double,

    val longitude: Double,

    /*
     * Android-reported GPS accuracy.
     */
    val accuracyMeters: Float,

    /*
     * Optional nearby landmark.
     */
    val landmark: String,

    /*
     * Optional user description.
     */
    val description: String,

    /*
     * ---------------------------------------------------------
     * PHOTO
     * ---------------------------------------------------------
     *
     * True when the report contains an attached photo.
     */
    val hasPhoto: Boolean,

    /*
     * Path to the photo saved inside OTO's
     * internal application storage.
     *
     * null means no photo was attached.
     */
    val photoPath: String? = null,

    /*
     * Time the report was created.
     *
     * Stored as milliseconds since epoch.
     */
    val createdAt: Long,

    /*
     * ---------------------------------------------------------
     * COMMUNITY VERIFICATION
     * ---------------------------------------------------------
     */

    /*
     * True once the report reaches OTO's required number
     * of unique confirmations.
     *
     * For the current prototype, two unique confirmations
     * can still mark the report as verified.
     */
    val isVerified: Boolean = false,

    /*
     * Number of UNIQUE users/devices that confirmed
     * the hazard is still present.
     *
     * This should match confirmedByIds.size.
     */
    val confirmationCount: Int = 0,

    /*
     * IDs belonging to the users/devices that have already
     * confirmed this report.
     *
     * During the current local prototype these can be
     * installation/device IDs.
     *
     * Once the onboarding/profile system is ready, these
     * can become actual user/profile IDs instead.
     *
     * This prevents one person from repeatedly pressing
     * STILL HERE and increasing the count forever.
     */
    val confirmedByIds: Set<String> = emptySet(),

    /*
     * Whether this report is still active.
     *
     * Resolved hazards eventually become false.
     */
    val isActive: Boolean = true
)


/*
 * -------------------------------------------------------------
 * PRIORITY CALCULATOR
 * -------------------------------------------------------------
 *
 * Users choose severity.
 *
 * OTO calculates priority automatically.
 */
fun calculateHazardPriority(
    reportType: String,
    severity: String
): HazardPriority {

    /*
     * ---------------------------------------------------------
     * CRITICAL TYPES
     * ---------------------------------------------------------
     */

    val criticalReportTypes =
        listOf(
            "Medical Emergency",
            "Lost Person",
            "Motor Vehicle Accident",
            "Bicycle Accident",
            "Hiking Injury",
            "Fall / Injury",
            "Wildfire / Smoke"
        )

    /*
     * Critical report types only become CRITICAL
     * when the user also selected High severity.
     */
    if (
        reportType in criticalReportTypes &&
        severity == "High"
    ) {

        return HazardPriority.CRITICAL
    }

    /*
     * ---------------------------------------------------------
     * HIGH-PRIORITY TYPES
     * ---------------------------------------------------------
     */

    val highPriorityReportTypes =
        listOf(
            "Bear Sighting",
            "Coyote Sighting",
            "Snake Sighting",
            "Animal Blocking Trail / Road",
            "Rockfall",
            "Landslide",
            "Road Blocked",
            "Trail Blocked",
            "Flooded Road",
            "Flooded Trail",
            "Bridge Damaged",
            "Broken Bridge",
            "Flooding",
            "Ice",
            "Heavy Snow",
            "High Winds",
            "Poor Visibility"
        )

    if (
        reportType in highPriorityReportTypes
    ) {

        if (
            severity == "High" ||
            severity == "Moderate"
        ) {

            return HazardPriority.HIGH
        }
    }

    /*
     * Any remaining hazard manually marked High
     * is still elevated.
     */
    if (
        severity == "High"
    ) {

        return HazardPriority.HIGH
    }

    /*
     * Everything else stays normal priority.
     */
    return HazardPriority.NORMAL
}