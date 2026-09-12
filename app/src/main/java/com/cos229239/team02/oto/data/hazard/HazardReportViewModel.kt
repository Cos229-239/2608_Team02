package com.cos229239.team02.oto.data.hazard

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel

/*
 * -------------------------------------------------------------
 * HAZARD REPORT VIEW MODEL
 * -------------------------------------------------------------
 *
 * Stores hazard reports for the app and keeps them
 * synchronized with local device storage.
 *
 * Reports now survive:
 *
 * - Screen changes
 * - Navigation
 * - App refreshes
 * - App restarts
 *
 * This is still local-device storage only.
 * A future backend will be needed for reports to
 * appear on other users' devices.
 */

class HazardReportViewModel(
    application: Application
) : AndroidViewModel(
    application
) {

    /*
     * ---------------------------------------------------------
     * LOCAL STORAGE
     * ---------------------------------------------------------
     */

    private val hazardStorage =
        HazardStorage(
            application
        )

    /*
     * ---------------------------------------------------------
     * REPORT LIST
     * ---------------------------------------------------------
     *
     * mutableStateListOf lets Compose automatically
     * update whenever a report changes.
     */

    private val _hazardReports =
        mutableStateListOf<HazardReport>()

    /*
     * Screens can read the reports,
     * but should make changes through this ViewModel.
     */
    val hazardReports: List<HazardReport>
        get() =
            _hazardReports

    /*
     * ---------------------------------------------------------
     * INITIAL LOAD
     * ---------------------------------------------------------
     *
     * Load reports previously saved on this device
     * whenever the ViewModel is created.
     */

    init {

        val savedReports =
            hazardStorage
                .loadReports()

        _hazardReports.addAll(
            savedReports
        )
    }

    /*
     * ---------------------------------------------------------
     * SAVE CURRENT REPORT LIST
     * ---------------------------------------------------------
     *
     * Central helper used whenever reports change.
     */

    private fun saveReports() {

        hazardStorage
            .saveReports(
                _hazardReports
            )
    }

    /*
     * ---------------------------------------------------------
     * SAVE HAZARD PHOTO
     * ---------------------------------------------------------
     *
     * Saves the Bitmap captured by the camera into
     * OTO's private app storage.
     *
     * Returns the file path if successful.
     */

    fun saveHazardPhoto(
        bitmap: Bitmap,
        reportId: String
    ): String? {

        return hazardStorage
            .savePhoto(
                bitmap =
                    bitmap,

                reportId =
                    reportId
            )
    }

    /*
     * ---------------------------------------------------------
     * ADD REPORT
     * ---------------------------------------------------------
     *
     * Called when the user presses Submit Report.
     */

    fun addHazardReport(
        report: HazardReport
    ) {

        _hazardReports.add(
            report
        )

        /*
         * Save the updated list immediately.
         */
        saveReports()
    }

    /*
     * ---------------------------------------------------------
     * ACTIVE REPORTS
     * ---------------------------------------------------------
     *
     * Only active hazards should count toward
     * the Explorer hazard total and map markers.
     */

    val activeHazardReports: List<HazardReport>
        get() =
            _hazardReports.filter {
                it.isActive
            }

    /*
     * ---------------------------------------------------------
     * ACTIVE HAZARD COUNT
     * ---------------------------------------------------------
     */

    val activeHazardCount: Int
        get() =
            activeHazardReports.size

    /*
     * ---------------------------------------------------------
     * MARK HAZARD RESOLVED
     * ---------------------------------------------------------
     *
     * Later this can be triggered by:
     *
     * - No longer here
     * - Official resolution
     * - Moderator action
     */

    fun markHazardResolved(
        reportId: String
    ) {

        val index =
            _hazardReports.indexOfFirst {
                it.id == reportId
            }

        if (
            index != -1
        ) {

            val currentReport =
                _hazardReports[
                    index
                ]

            _hazardReports[
                index
            ] =
                currentReport.copy(
                    isActive =
                        false
                )

            /*
             * Save the change.
             */
            saveReports()
        }
    }

    /*
     * ---------------------------------------------------------
     * CONFIRM HAZARD
     * ---------------------------------------------------------
     *
     * Adds one community confirmation.
     */

    fun confirmHazard(
        reportId: String
    ) {

        val index =
            _hazardReports.indexOfFirst {
                it.id == reportId
            }

        if (
            index != -1
        ) {

            val currentReport =
                _hazardReports[
                    index
                ]

            val newConfirmationCount =
                currentReport.confirmationCount +
                        1

            _hazardReports[
                index
            ] =
                currentReport.copy(

                    confirmationCount =
                        newConfirmationCount,

                    /*
                     * For the prototype,
                     * two confirmations make
                     * the report verified.
                     */
                    isVerified =
                        newConfirmationCount >= 2
                )

            /*
             * Save the change.
             */
            saveReports()
        }
    }

    /*
     * ---------------------------------------------------------
     * MARK REPORT INCORRECT
     * ---------------------------------------------------------
     *
     * For now, an incorrect report is simply
     * made inactive.
     *
     * Later we can keep a separate moderation state.
     */

    fun markHazardIncorrect(
        reportId: String
    ) {

        val index =
            _hazardReports.indexOfFirst {
                it.id == reportId
            }

        if (
            index != -1
        ) {

            val currentReport =
                _hazardReports[
                    index
                ]

            _hazardReports[
                index
            ] =
                currentReport.copy(
                    isActive =
                        false
                )

            saveReports()
        }
    }

    /*
     * ---------------------------------------------------------
     * REMOVE REPORT
     * ---------------------------------------------------------
     *
     * Permanently removes a report from local storage.
     *
     * If that report had a saved photo,
     * the photo is deleted too.
     */

    fun removeHazardReport(
        reportId: String
    ) {

        val report =
            _hazardReports
                .firstOrNull {
                    it.id == reportId
                }

        if (
            report != null
        ) {

            /*
             * Delete saved photo first.
             */
            hazardStorage
                .deletePhoto(
                    report.photoPath
                )

            /*
             * Remove report.
             */
            _hazardReports.removeAll {
                it.id == reportId
            }

            /*
             * Save updated list.
             */
            saveReports()
        }
    }

    /*
     * ---------------------------------------------------------
     * CLEAR ALL REPORTS
     * ---------------------------------------------------------
     *
     * Mainly useful for emulator testing.
     *
     * This removes:
     *
     * - Report JSON
     * - Saved photos
     * - Current ViewModel report list
     */

    fun clearHazardReports() {

        _hazardReports.clear()

        hazardStorage
            .clearAllStorage()
    }
}