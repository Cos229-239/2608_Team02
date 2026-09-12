package com.cos229239.team02.oto.data.hazard

import android.content.Context
import android.graphics.Bitmap
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
 * This class does two main things:
 *
 * 1. Saves and loads hazard report information.
 * 2. Saves hazard report photos to app storage.
 *
 * This allows reports to survive an app restart.
 *
 * This is still LOCAL storage only.
 * A future backend will be needed to share reports
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
     * REPORT FILE
     * ---------------------------------------------------------
     *
     * Hazard report information is saved into one
     * JSON file inside OTO's private app storage.
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
     *
     * Each report photo is saved as its own JPEG file.
     */

    private val photoDirectory =
        File(
            appContext.filesDir,
            "hazard_photos"
        )

    init {

        /*
         * Create the photo folder if it does
         * not already exist.
         */
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
     *
     * Saves the Bitmap returned by the camera.
     *
     * Returns the saved file path.
     *
     * If something goes wrong, returns null.
     */

    fun savePhoto(
        bitmap: Bitmap,
        reportId: String
    ): String? {

        return try {

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

    /*
     * ---------------------------------------------------------
     * DELETE PHOTO
     * ---------------------------------------------------------
     *
     * Useful later when a report is permanently removed.
     */

    fun deletePhoto(
        photoPath: String?
    ) {

        if (
            photoPath.isNullOrBlank()
        ) {

            return
        }

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

    /*
     * ---------------------------------------------------------
     * SAVE REPORTS
     * ---------------------------------------------------------
     *
     * Converts every HazardReport into JSON and writes
     * the complete list to local app storage.
     */

    fun saveReports(
        reports: List<HazardReport>
    ) {

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
                 * JSONObject.NULL represents an actual
                 * null value inside JSON.
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

                jsonReport.put(
                    "confirmationCount",
                    report.confirmationCount
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

    /*
     * ---------------------------------------------------------
     * LOAD REPORTS
     * ---------------------------------------------------------
     *
     * Reads the saved JSON file and rebuilds the
     * HazardReport objects when OTO starts.
     */

    fun loadReports(): List<HazardReport> {

        /*
         * First launch:
         * no saved report file exists yet.
         */
        if (
            !reportsFile.exists()
        ) {

            return emptyList()
        }

        return try {

            val fileText =
                reportsFile.readText()

            if (
                fileText.isBlank()
            ) {

                return emptyList()
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
                 *
                 * If an invalid priority somehow exists,
                 * fall back to NORMAL instead of crashing.
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

                /*
                 * If the file no longer exists,
                 * treat the report as having no photo.
                 */
                val validPhotoPath =
                    if (
                        !photoPath.isNullOrBlank() &&
                        File(photoPath).exists()
                    ) {

                        photoPath

                    } else {

                        null
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

                        isVerified =
                            jsonReport.optBoolean(
                                "isVerified",
                                false
                            ),

                        confirmationCount =
                            jsonReport.optInt(
                                "confirmationCount",
                                0
                            ),

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

    /*
     * ---------------------------------------------------------
     * CLEAR EVERYTHING
     * ---------------------------------------------------------
     *
     * Mainly useful during emulator testing.
     */

    fun clearAllStorage() {

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