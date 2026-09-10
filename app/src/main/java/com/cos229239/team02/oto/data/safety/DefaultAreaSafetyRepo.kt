package com.cos229239.team02.oto.data.safety

import com.cos229239.team02.oto.data.location.OtoLocation
import com.cos229239.team02.oto.data.resource.NpsAlertClient
import com.cos229239.team02.oto.data.resource.NwsAlertClient
import com.cos229239.team02.oto.data.resource.ResourceRepository
import com.cos229239.team02.oto.data.resource.ResourceResult
import com.cos229239.team02.oto.data.safety.SafetyNotification
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

//Collects NPS and NWS updates and converts to app safety notifications after removing dummy repo
class DefaultAreaSafetyRepo(
    private val resourceRepository: ResourceRepository,
    private val nwsClient: NwsAlertClient,
    private val npsClient: NpsAlertClient,

) : AreaSafetyRepo {

    override suspend fun getAreaSafety(
        location: OtoLocation,
        areaName: String,
        parkCode: String?,
        forceRefresh: Boolean
    ): AreaSafetyData = supervisorScope {

        val weatherRequest = async {
            captureSafetyRequest {
                nwsClient.getAlerts(location)
        }
    }

val parkRequest = async {
        if (parkCode.isNullOrBlank()) {
            null
        }else {
        captureSafetyRequest {
            npsClient.getAlerts(parkCode)
        }
    }
}
        val resourceRequest = async {
                captureSafetyRequest {
                    withContext(Dispatchers.IO) {
                        resourceRepository.getNearby(
                            location = location,
                            forceRefresh = forceRefresh
                        )
                    }
                }
            }


        val weatherResult = weatherRequest.await()
        val parkResult = parkRequest.await()
        val nearbyResult = resourceRequest.await()


        val sources = listOf(
            alertStatus(
                source = "NWS",
                result = weatherResult
            ),

            parkResult?.let {
                alertStatus(
                    source = "NPS",
                    result = it
                )
            } ?: SafetySourceStatus(
                source = "NPS",
                state = SafetySourceState.SKIPPED,
                message = "Select a national park to check park notices."
            ),

            resourceStatus(nearbyResult)
        )

            val notifications = (
                    weatherResult.getOrDefault(emptyList()) +
                    parkResult?.getOrDefault(emptyList()).orEmpty()

                    )
                .distinctBy { it.id }
                .sortedBy { it.level.ordinal }

        AreaSafetyData(
            areaName = areaName,
            notifications = notifications,
            resourceResult = nearbyResult.getOrNull(),
            sources = sources
              )
            }

    private fun alertStatus(
        source: String,
        result: Result<List<SafetyNotification>>
    ): SafetySourceStatus = result.fold(
        onSuccess = { notifications ->
            SafetySourceStatus(
                source = source,
                state = SafetySourceState.SUCCESS,
                message = "${notifications.size} notices returned."
            )
        },
        onFailure = { error ->
            SafetySourceStatus(
                source = source,
                state = SafetySourceState.FAILED,
                message = safetyErrorMessage(error)
            )
        }
    )

    private fun resourceStatus(
        result: Result<ResourceResult>
    ): SafetySourceStatus = result.fold(
        onSuccess = { data ->
            val error = data.error

            when {
                data.fromCache -> SafetySourceStatus(
                    source = "Overpass",
                    state = SafetySourceState.CACHED,
                    message = error
                        ?: "Showing saved nearby resources."
                )

                error != null -> SafetySourceStatus(
                    source = "Overpass",
                    state = SafetySourceState.FAILED,
                    message = error
                )
                else -> SafetySourceStatus(
                    source = "Overpass",
                    state = SafetySourceState.SUCCESS,
                    message = "${data.resources.size} resources returned."
                )
            }
        },
        onFailure = { error ->
            SafetySourceStatus(
                source = "Overpass",
                state = SafetySourceState.FAILED,
                message = safetyErrorMessage(error)
            )
        }
    )
}
internal suspend fun <T> captureSafetyRequest(
    block: suspend () -> T
): Result<T> = try {
    Result.success(block())
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    Result.failure(error)
}