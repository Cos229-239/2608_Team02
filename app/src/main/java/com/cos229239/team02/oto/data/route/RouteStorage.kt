package com.cos229239.team02.oto.data.route

import android.content.Context
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

            runCatching {

                val contents =
                    json.encodeToString(route)

                File(
                    routesDir,
                    "${route.id}.json"
                ).writeText(
                    contents
                )

                latestRouteFile.writeText(
                    contents
                )
            }.isSuccess
        }

    /**
     * Loads the most recently saved route, if one exists.
     */
    suspend fun loadLatest(): TrackedRoute? =
        withContext(Dispatchers.IO) {

            runCatching {

                if (latestRouteFile.exists()) {
                    json.decodeFromString(
                        latestRouteFile.readText()
                    )
                } else {
                    null
                }
            }.getOrNull()
        }

    companion object {

        private const val ROUTES_DIR_NAME = "oto_routes"

        private const val LATEST_ROUTE_FILE_NAME = "latest.json"
    }
}