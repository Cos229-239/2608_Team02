package com.cos229239.team02.oto.data.hazard

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
 * -------------------------------------------------------------
 * HAZARD REPORT VIEW MODEL
 * -------------------------------------------------------------
 *
 * Stores hazard reports for the app and keeps them
 * synchronized with local device storage.
 *
 * Reports survive:
 *
 * - Screen changes
 * - Navigation
 * - App refreshes
 * - App restarts
 *
 * Hazard file operations are handled asynchronously so
 * report actions do not block the main UI thread.
 *
 * Community confirmations currently use a persistent
 * installation ID.
 *
 * Later, when OTO's onboarding/profile system is ready,
 * this installation ID can be replaced with the user's
 * real profile/account ID.
 *
 * This is still local-device storage only.
 * A future backend will be needed for reports to appear
 * on other users' devices.
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
     * PERSISTENCE MUTEX
     * ---------------------------------------------------------
     *
     * Keeps higher-level persistence operations ordered.
     *
     * HazardStorage also has its own file Mutex.
     */
    private val persistenceMutex =
        Mutex()

    /*
     * ---------------------------------------------------------
     * INSTALLATION ID
     * ---------------------------------------------------------
     *
     * This acts as the temporary identity for community
     * confirmations.
     *
     * The value is created once and stored in SharedPreferences,
     * so restarting the app does NOT create a new ID.
     *
     * This prevents the same app installation from repeatedly
     * pressing STILL HERE on the same report.
     *
     * Later:
     *
     * installation ID
     *
     * can be replaced with:
     *
     * profile ID / user ID
     */
    private val installationId: String =
        getOrCreateInstallationId(
            application
        )

    /*
     * ---------------------------------------------------------
     * REPORT LIST
     * ---------------------------------------------------------
     */

    private val _hazardReports =
        mutableStateListOf<HazardReport>()

    /*
     * Screens can read the reports,
     * but changes should happen through this ViewModel.
     */
    val hazardReports: List<HazardReport>
        get() =
            _hazardReports

    /*
     * ---------------------------------------------------------
     * FIELD REPORT SELECTION
     * ---------------------------------------------------------
     */

    private val _selectedFieldReportIds =
        mutableStateOf<Set<String>>(
            emptySet()
        )

    val selectedFieldReportIds: Set<String>
        get() =
            _selectedFieldReportIds.value

    /*
     * ---------------------------------------------------------
     * INITIAL LOAD
     * ---------------------------------------------------------
     *
     * HazardStorage performs the actual JSON read and parsing
     * on Dispatchers.IO.
     */

    init {

        viewModelScope.launch {

            val savedReports =
                hazardStorage
                    .loadReports()

            _hazardReports.clear()

            _hazardReports.addAll(
                savedReports
            )
        }
    }

    /*
     * ---------------------------------------------------------
     * SAVE CURRENT REPORT LIST
     * ---------------------------------------------------------
     *
     * Persistence happens asynchronously.
     *
     * The report snapshot is created inside the Mutex so
     * queued saves always write the newest state available
     * when their turn begins.
     */

    private fun saveReports() {

        viewModelScope.launch {

            persistenceMutex.withLock {

                val reportsSnapshot =
                    _hazardReports.toList()

                hazardStorage
                    .saveReports(
                        reportsSnapshot
                    )
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * SAVE HAZARD PHOTO
     * ---------------------------------------------------------
     *
     * JPEG compression and file writing are handled by
     * HazardStorage on Dispatchers.IO.
     */

    suspend fun saveHazardPhoto(
        bitmap: Bitmap,
        reportId: String
    ): String? {

        return persistenceMutex.withLock {

            hazardStorage
                .savePhoto(
                    bitmap =
                        bitmap,

                    reportId =
                        reportId
                )
        }
    }

    /*
     * ---------------------------------------------------------
     * ADD REPORT
     * ---------------------------------------------------------
     */

    fun addHazardReport(
        report: HazardReport
    ) {

        _hazardReports.add(
            report
        )

        saveReports()
    }

    /*
     * ---------------------------------------------------------
     * ACTIVE REPORTS
     * ---------------------------------------------------------
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
     * SELECT FIELD REPORTS
     * ---------------------------------------------------------
     */

    fun selectFieldReports(
        reports: List<HazardReport>
    ) {

        _selectedFieldReportIds.value =
            reports
                .map {
                    it.id
                }
                .toSet()
    }

    /*
     * ---------------------------------------------------------
     * CLEAR FIELD REPORT SELECTION
     * ---------------------------------------------------------
     */

    fun clearFieldReportSelection() {

        _selectedFieldReportIds.value =
            emptySet()
    }

    /*
     * ---------------------------------------------------------
     * SELECTED FIELD REPORTS
     * ---------------------------------------------------------
     */

    val selectedFieldReports: List<HazardReport>
        get() {

            val selectedIds =
                _selectedFieldReportIds.value

            return if (
                selectedIds.isEmpty()
            ) {

                activeHazardReports

            } else {

                activeHazardReports.filter {
                    it.id in selectedIds
                }
            }
        }

    /*
     * ---------------------------------------------------------
     * MARK HAZARD RESOLVED
     * ---------------------------------------------------------
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

            saveReports()
        }
    }

    /*
     * ---------------------------------------------------------
     * CONFIRM HAZARD
     * ---------------------------------------------------------
     *
     * One installation/profile may confirm each report once.
     *
     * Example:
     *
     * Device A -> count becomes 1
     * Device A again -> count stays 1
     * Device B -> count becomes 2
     * Device C -> count becomes 3
     *
     * There is NO maximum total confirmation count.
     */

    fun confirmHazard(
        reportId: String
    ) {

        val index =
            _hazardReports.indexOfFirst {
                it.id == reportId
            }

        if (
            index == -1
        ) {

            return
        }

        val currentReport =
            _hazardReports[
                index
            ]

        /*
         * -----------------------------------------------------
         * ALREADY CONFIRMED
         * -----------------------------------------------------
         *
         * This installation has already confirmed
         * the report.
         *
         * Do not change the report.
         */
        if (
            installationId in
            currentReport.confirmedByIds
        ) {

            return
        }

        /*
         * Add this installation to the unique
         * confirmer list.
         */
        val updatedConfirmedByIds =
            currentReport.confirmedByIds +
                    installationId

        /*
         * -----------------------------------------------------
         * NEW CONFIRMATION COUNT
         * -----------------------------------------------------
         *
         * New reports normally keep confirmationCount equal
         * to confirmedByIds.size.
         *
         * maxOf also protects older locally saved reports that
         * may already contain a legacy confirmation count but
         * did not have confirmer IDs.
         */
        val newConfirmationCount =
            maxOf(
                currentReport.confirmationCount,
                currentReport.confirmedByIds.size
            ) + 1

        _hazardReports[
            index
        ] =
            currentReport.copy(

                confirmationCount =
                    newConfirmationCount,

                confirmedByIds =
                    updatedConfirmedByIds,

                /*
                 * Two UNIQUE confirmations currently mark
                 * the report verified.
                 *
                 * Verification does NOT stop the count from
                 * continuing to increase.
                 */
                isVerified =
                    newConfirmationCount >= 2
            )

        saveReports()
    }

    /*
     * ---------------------------------------------------------
     * HAS CURRENT INSTALLATION CONFIRMED
     * ---------------------------------------------------------
     *
     * Useful for UI later.
     *
     * For example:
     *
     * STILL HERE
     *
     * could change to:
     *
     * ✓ YOU CONFIRMED THIS
     */
    fun hasCurrentInstallationConfirmed(
        reportId: String
    ): Boolean {

        val report =
            _hazardReports
                .firstOrNull {
                    it.id == reportId
                }

        return report != null &&
                installationId in
                report.confirmedByIds
    }

    /*
     * ---------------------------------------------------------
     * MARK REPORT INCORRECT
     * ---------------------------------------------------------
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
     * Permanently removes a report locally.
     *
     * The report disappears from Compose immediately.
     *
     * Photo deletion and JSON persistence happen
     * asynchronously.
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
            report == null
        ) {

            return
        }

        /*
         * Remove report from UI state.
         */
        _hazardReports.removeAll {
            it.id == reportId
        }

        /*
         * Remove the deleted report from the
         * Field Reports selection too.
         */
        _selectedFieldReportIds.value =
            _selectedFieldReportIds.value -
                    reportId

        viewModelScope.launch {

            persistenceMutex.withLock {

                /*
                 * Delete the saved photo.
                 */
                hazardStorage
                    .deletePhoto(
                        report.photoPath
                    )

                /*
                 * Capture the current list after deletion.
                 */
                val reportsSnapshot =
                    _hazardReports.toList()

                /*
                 * Save the updated report list.
                 */
                hazardStorage
                    .saveReports(
                        reportsSnapshot
                    )
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * CLEAR ALL REPORTS
     * ---------------------------------------------------------
     */

    fun clearHazardReports() {

        _hazardReports.clear()

        _selectedFieldReportIds.value =
            emptySet()

        viewModelScope.launch {

            persistenceMutex.withLock {

                hazardStorage
                    .clearAllStorage()
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * INSTALLATION ID HELPER
     * ---------------------------------------------------------
     *
     * Generates an ID once and saves it using Android
     * SharedPreferences.
     *
     * The same installation will continue using this ID
     * across app restarts.
     *
     * Uninstalling/reinstalling the app can create a new ID,
     * which is acceptable for this temporary prototype.
     *
     * Once profiles are available, confirmation identity
     * should come from the signed-in user/profile instead.
     */

    private fun getOrCreateInstallationId(
        application: Application
    ): String {

        val preferences =
            application.getSharedPreferences(
                INSTALLATION_PREFERENCES_NAME,
                Application.MODE_PRIVATE
            )

        val existingId =
            preferences.getString(
                INSTALLATION_ID_KEY,
                null
            )

        if (
            !existingId.isNullOrBlank()
        ) {

            return existingId
        }

        val newId =
            UUID.randomUUID()
                .toString()

        preferences
            .edit()
            .putString(
                INSTALLATION_ID_KEY,
                newId
            )
            .apply()

        return newId
    }

    companion object {

        private const val INSTALLATION_PREFERENCES_NAME =
            "oto_hazard_identity"

        private const val INSTALLATION_ID_KEY =
            "installation_id"
    }
}