package com.cos229239.team02.oto.data.hazard

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/*
 * -------------------------------------------------------------
 * HAZARD STORAGE
 * -------------------------------------------------------------
 *
 * Handles local storage for hazard reports.
 *
 * All file and photo work is performed on Dispatchers.IO
 * so disk operations do not block the Compose UI thread.
 *
 * A Mutex protects local hazard storage so multiple save,
 * delete, or clear operations do not write to the files
 * at the same time.
 *
 * This remains LOCAL storage only.
 * A future backend will be required to share reports
 * between different OTO users/devices.
 */

class HazardStorage(
    context: Context
) {

    /*
     * Use the application context so this storage
     * class does not hold onto an Activity.
     */
    private val appContext =
        context.applicationContext

    /*
     * ---------------------------------------------------------
     * STORAGE MUTEX
     * ---------------------------------------------------------
     */

    private val storageMutex =
        Mutex()

    /*
     * ---------------------------------------------------------
     * REPORT FILE
     * ---------------------------------------------------------
     */

    private val reportsFile =
        File(
            appContext.filesDir,
            "hazard_reports.json"
        )

    /*
     * ---------------------------------------------------------
     * PHOTO DIRECTORY
     * ---------------------------------------------------------
     */

    private val photoDirectory =
        File(
            appContext.filesDir,
            "hazard_photos"
        )

    init {

        if (
            !photoDirectory.exists()
        ) {

            photoDirectory.mkdirs()
        }
    }

    /*
     * ---------------------------------------------------------
     * SAVE PHOTO
     * ---------------------------------------------------------
     */

    suspend fun savePhoto(
        bitmap: Bitmap,
        reportId: String
    ): String? {

        return withContext(
            Dispatchers.IO
        ) {

            storageMutex.withLock {

                try {

                    if (
                        !photoDirectory.exists()
                    ) {

                        photoDirectory.mkdirs()
                    }

                    val photoFile =
                        File(
                            photoDirectory,
                            "$reportId.jpg"
                        )

                    FileOutputStream(
                        photoFile
                    ).use { outputStream ->

                        bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            90,
                            outputStream
                        )
                    }

                    photoFile.absolutePath

                } catch (
                    exception: Exception
                ) {

                    exception.printStackTrace()

                    null
                }
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * DELETE PHOTO
     * ---------------------------------------------------------
     */

    suspend fun deletePhoto(
        photoPath: String?
    ) {

        if (
            photoPath.isNullOrBlank()
        ) {

            return
        }

        withContext(
            Dispatchers.IO
        ) {

            storageMutex.withLock {

                try {

                    val photoFile =
                        File(
                            photoPath
                        )

                    if (
                        photoFile.exists()
                    ) {

                        photoFile.delete()
                    }

                } catch (
                    exception: Exception
                ) {

                    exception.printStackTrace()
                }
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * SAVE REPORTS
     * ---------------------------------------------------------
     *
     * Converts every HazardReport into JSON and writes
     * the complete list to local app storage.
     */

    suspend fun saveReports(
        reports: List<HazardReport>
    ) {

        withContext(
            Dispatchers.IO
        ) {

            storageMutex.withLock {

                try {

                    val jsonArray =
                        JSONArray()

                    reports.forEach { report ->

                        val jsonReport =
                            JSONObject()

                        jsonReport.put(
                            "id",
                            report.id
                        )

                        jsonReport.put(
                            "category",
                            report.category
                        )

                        jsonReport.put(
                            "reportType",
                            report.reportType
                        )

                        jsonReport.put(
                            "severity",
                            report.severity
                        )

                        jsonReport.put(
                            "priority",
                            report.priority.name
                        )

                        jsonReport.put(
                            "latitude",
                            report.latitude
                        )

                        jsonReport.put(
                            "longitude",
                            report.longitude
                        )

                        jsonReport.put(
                            "accuracyMeters",
                            report.accuracyMeters
                        )

                        jsonReport.put(
                            "landmark",
                            report.landmark
                        )

                        jsonReport.put(
                            "description",
                            report.description
                        )

                        jsonReport.put(
                            "hasPhoto",
                            report.hasPhoto
                        )

                        /*
                         * JSONObject.NULL represents an
                         * actual null value inside JSON.
                         */
                        if (
                            report.photoPath == null
                        ) {

                            jsonReport.put(
                                "photoPath",
                                JSONObject.NULL
                            )

                        } else {

                            jsonReport.put(
                                "photoPath",
                                report.photoPath
                            )
                        }

                        jsonReport.put(
                            "createdAt",
                            report.createdAt
                        )

                        jsonReport.put(
                            "isVerified",
                            report.isVerified
                        )

                        /*
                         * -------------------------------------------------
                         * CONFIRMATION COUNT
                         * -------------------------------------------------
                         *
                         * Kept as a stored value for easy display.
                         */
                        jsonReport.put(
                            "confirmationCount",
                            report.confirmationCount
                        )

                        /*
                         * -------------------------------------------------
                         * UNIQUE CONFIRMERS
                         * -------------------------------------------------
                         *
                         * Stores every device/profile ID that has
                         * already confirmed this report.
                         *
                         * This prevents one device/profile from
                         * repeatedly increasing the count.
                         */
                        val confirmedByJsonArray =
                            JSONArray()

                        report.confirmedByIds
                            .forEach { confirmerId ->

                                confirmedByJsonArray.put(
                                    confirmerId
                                )
                            }

                        jsonReport.put(
                            "confirmedByIds",
                            confirmedByJsonArray
                        )

                        jsonReport.put(
                            "isActive",
                            report.isActive
                        )

                        jsonArray.put(
                            jsonReport
                        )
                    }

                    reportsFile.writeText(
                        jsonArray.toString()
                    )

                } catch (
                    exception: Exception
                ) {

                    exception.printStackTrace()
                }
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * LOAD REPORTS
     * ---------------------------------------------------------
     *
     * Reads the saved JSON file and rebuilds the
     * HazardReport objects when OTO starts.
     */

    suspend fun loadReports(): List<HazardReport> {

        return withContext(
            Dispatchers.IO
        ) {

            storageMutex.withLock {

                if (
                    !reportsFile.exists()
                ) {

                    return@withLock emptyList()
                }

                try {

                    val fileText =
                        reportsFile.readText()

                    if (
                        fileText.isBlank()
                    ) {

                        return@withLock emptyList()
                    }

                    val jsonArray =
                        JSONArray(
                            fileText
                        )

                    val loadedReports =
                        mutableListOf<HazardReport>()

                    for (
                    index in 0 until jsonArray.length()
                    ) {

                        val jsonReport =
                            jsonArray.getJSONObject(
                                index
                            )

                        /*
                         * -------------------------------------------------
                         * PRIORITY
                         * -------------------------------------------------
                         */

                        val priority =
                            try {

                                HazardPriority.valueOf(
                                    jsonReport.optString(
                                        "priority",
                                        HazardPriority.NORMAL.name
                                    )
                                )

                            } catch (
                                exception: IllegalArgumentException
                            ) {

                                HazardPriority.NORMAL
                            }

                        /*
                         * -------------------------------------------------
                         * PHOTO PATH
                         * -------------------------------------------------
                         */

                        val photoPath =
                            if (
                                jsonReport.isNull(
                                    "photoPath"
                                )
                            ) {

                                null

                            } else {

                                jsonReport.optString(
                                    "photoPath"
                                )
                            }

                        val validPhotoPath =
                            if (
                                !photoPath.isNullOrBlank() &&
                                File(
                                    photoPath
                                ).exists()
                            ) {

                                photoPath

                            } else {

                                null
                            }

                        /*
                         * -------------------------------------------------
                         * UNIQUE CONFIRMERS
                         * -------------------------------------------------
                         *
                         * Older saved report files will not contain
                         * confirmedByIds yet.
                         *
                         * In that case we simply load an empty set.
                         */
                        val confirmedByIds =
                            mutableSetOf<String>()

                        val confirmedByJsonArray =
                            jsonReport.optJSONArray(
                                "confirmedByIds"
                            )

                        if (
                            confirmedByJsonArray != null
                        ) {

                            for (
                            confirmerIndex in 0 until
                                    confirmedByJsonArray.length()
                            ) {

                                val confirmerId =
                                    confirmedByJsonArray
                                        .optString(
                                            confirmerIndex
                                        )

                                if (
                                    confirmerId.isNotBlank()
                                ) {

                                    confirmedByIds.add(
                                        confirmerId
                                    )
                                }
                            }
                        }

                        /*
                         * -------------------------------------------------
                         * CONFIRMATION COUNT
                         * -------------------------------------------------
                         *
                         * New reports should use the size of
                         * confirmedByIds.
                         *
                         * For older saved reports that do not yet have
                         * confirmer IDs, preserve the old count so
                         * existing test data does not suddenly become 0.
                         */
                        val storedConfirmationCount =
                            jsonReport.optInt(
                                "confirmationCount",
                                0
                            )

                        val confirmationCount =
                            if (
                                confirmedByJsonArray != null
                            ) {

                                confirmedByIds.size

                            } else {

                                storedConfirmationCount
                            }

                        val report =
                            HazardReport(

                                id =
                                    jsonReport.optString(
                                        "id"
                                    ),

                                category =
                                    jsonReport.optString(
                                        "category"
                                    ),

                                reportType =
                                    jsonReport.optString(
                                        "reportType"
                                    ),

                                severity =
                                    jsonReport.optString(
                                        "severity"
                                    ),

                                priority =
                                    priority,

                                latitude =
                                    jsonReport.optDouble(
                                        "latitude"
                                    ),

                                longitude =
                                    jsonReport.optDouble(
                                        "longitude"
                                    ),

                                accuracyMeters =
                                    jsonReport.optDouble(
                                        "accuracyMeters",
                                        0.0
                                    ).toFloat(),

                                landmark =
                                    jsonReport.optString(
                                        "landmark"
                                    ),

                                description =
                                    jsonReport.optString(
                                        "description"
                                    ),

                                hasPhoto =
                                    validPhotoPath != null,

                                photoPath =
                                    validPhotoPath,

                                createdAt =
                                    jsonReport.optLong(
                                        "createdAt"
                                    ),

                                /*
                                 * Verification is recalculated from
                                 * the loaded unique confirmation count.
                                 *
                                 * Two unique confirmations still mark
                                 * a report verified for the prototype.
                                 */
                                isVerified =
                                    confirmationCount >= 2,

                                confirmationCount =
                                    confirmationCount,

                                confirmedByIds =
                                    confirmedByIds.toSet(),

                                isActive =
                                    jsonReport.optBoolean(
                                        "isActive",
                                        true
                                    )
                            )

                        loadedReports.add(
                            report
                        )
                    }

                    loadedReports

                } catch (
                    exception: Exception
                ) {

                    exception.printStackTrace()

                    emptyList()
                }
            }
        }
    }

    /*
     * ---------------------------------------------------------
     * CLEAR EVERYTHING
     * ---------------------------------------------------------
     */

    suspend fun clearAllStorage() {

        withContext(
            Dispatchers.IO
        ) {

            storageMutex.withLock {

                try {

                    /*
                     * Delete report JSON.
                     */
                    if (
                        reportsFile.exists()
                    ) {

                        reportsFile.delete()
                    }

                    /*
                     * Delete every saved hazard photo.
                     */
                    if (
                        photoDirectory.exists()
                    ) {

                        photoDirectory
                            .listFiles()
                            ?.forEach { file ->

                                file.delete()
                            }
                    }

                } catch (
                    exception: Exception
                ) {

                    exception.printStackTrace()
                }
            }
        }
    }
}