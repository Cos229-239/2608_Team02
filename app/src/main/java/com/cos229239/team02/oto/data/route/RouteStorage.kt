package com.cos229239.team02.oto.data.route

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Saves and loads tracked routes as JSON files in app-private storage,
 * so the last route survives app restarts and can be backtracked offline.
 */
class RouteStorage(
    context: Context
) {

    private val routesDir =
        File(
            context.filesDir,
            ROUTES_DIR_NAME
        ).apply {
            mkdirs()
        }

    private val latestRouteFile =
        File(
            routesDir,
            LATEST_ROUTE_FILE_NAME
        )

    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    /**
     * Writes the route to a per-route file and marks it as the latest route.
     */
    suspend fun save(
        route: TrackedRoute
    ): Boolean =
        withContext(Dispatchers.IO) {
            saveBlocking(route)
        }

    /**
     * Writes the route synchronously on the calling thread. Used at
     * moments (like app backgrounding) where an async write could be
     * cancelled by process death before it reaches disk.
     */
    fun saveBlocking(
        route: TrackedRoute
    ): Boolean =

        runCatching {

            val contents =
                json.encodeToString(route)

            File(
                routesDir,
                "${route.id}.json"
            ).writeText(
                contents
            )

            writeLatestAtomically(
                contents
            )

        }.onFailure { error ->
            Log.e(
                TAG,
                "Could not save route",
                error
            )
        }.isSuccess

    /**
     * Whether a saved route exists for the next session.
     */
    fun latestRouteExists(): Boolean =
        latestRouteFile.exists()

    /**
     * Deletes all saved routes so the app starts fresh.
     */
    fun clearSavedRoutes() {

        runCatching {

            latestRouteFile.delete()
            File(routesDir, LATEST_TEMP_FILE_NAME).delete()

            routesDir.listFiles { file ->
                file.name.endsWith(ROUTE_FILE_EXTENSION)
            }
                .orEmpty()
                .forEach { file ->
                    file.delete()
                }

        }.onFailure { error ->
            Log.e(
                TAG,
                "Could not clear saved routes",
                error
            )
        }
    }

    /**
     * Loads the most recently saved route, if one exists. If the
     * latest.json file is corrupt or was cut short (for example when
     * the process was killed mid-write), falls back to the newest
     * intact per-route file.
     */
    suspend fun loadLatest(): TrackedRoute? =
        withContext(Dispatchers.IO) {

            val fromLatest =
                decodeFile(latestRouteFile)

            if (fromLatest != null) {
                fromLatest
            } else {
                newestRouteFile()
            }
        }

    /**
     * Writes the latest marker file via a temp file + rename so a
     * partially-written marker can never be observed (a process kill
     * mid-write would otherwise leave a truncated latest.json).
     */
    private fun writeLatestAtomically(
        contents: String
    ) {

        val tempFile =
            File(
                routesDir,
                LATEST_TEMP_FILE_NAME
            )

        tempFile.writeText(contents)

        latestRouteFile.delete()

        if (!tempFile.renameTo(latestRouteFile)) {

            latestRouteFile.writeText(contents)
        }
    }

    /**
     * Tries each per-route file, newest first, and returns the first
     * one that decodes cleanly.
     */
    private fun newestRouteFile(): TrackedRoute? {

        val candidates =
            routesDir.listFiles { file ->
                file.name.endsWith(ROUTE_FILE_EXTENSION) &&
                    file != latestRouteFile
            }
                .orEmpty()
                .sortedByDescending { file ->
                    file.lastModified()
                }

        for (file in candidates) {

            val route =
                decodeFile(file)

            if (route != null) {
                return route
            }
        }

        return null
    }

    /**
     * Decodes a route file, logging the reason if it can't be read so
     * failures are visible in logcat.
     */
    private fun decodeFile(
        file: File
    ): TrackedRoute? {

        if (!file.exists()) {
            return null
        }

        return runCatching {

            json.decodeFromString<TrackedRoute>(
                file.readText()
            )

        }.onFailure { error ->

            Log.e(
                TAG,
                "Could not read route file ${file.name} " +
                    "(${file.length()} bytes)",
                error
            )

        }.getOrNull()
    }

    companion object {

        private const val ROUTES_DIR_NAME = "oto_routes"

        private const val LATEST_ROUTE_FILE_NAME = "latest.json"

        private const val LATEST_TEMP_FILE_NAME = "latest.json.tmp"

        private const val ROUTE_FILE_EXTENSION = ".json"

        private const val TAG = "RouteStorage"
    }
}