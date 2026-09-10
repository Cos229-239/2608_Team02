package com.cos229239.team02.oto.data.route

import kotlinx.serialization.Serializable

/**
 * A recorded path of GPS points that can be saved and loaded from disk.
 */
@Serializable
data class TrackedRoute(
    val id: String,
    val startedAtMillis: Long,
    val endedAtMillis: Long? = null,
    val points: List<RoutePoint>
)