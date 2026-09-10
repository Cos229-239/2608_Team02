package com.cos229239.team02.oto.data.safety

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


internal class SafetyHttpException(
    val statusCode: Int
) : IOException("HTTP $statusCode")

fun createSafetyHttpClient(): OkHttpClient =
    OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

internal suspend fun OkHttpClient.getSafetyJson(
    request: Request
): JSONObject = suspendCancellableCoroutine { continuation ->
    val call = newCall(request)

    continuation.invokeOnCancellation {
        call.cancel()
    }

    call.enqueue(object : Callback {
        override fun onFailure(
            call: Call,
            e: IOException
        ) {
            if (continuation.isActive) {
                continuation.resumeWithException(e)
            }
        }

        override fun onResponse(
            call: Call,
            response: Response
        ) {
            try {
                val json = response.use {
                    if (!it.isSuccessful) {
                        throw SafetyHttpException(it.code)
                    }
                    val body = it.body?.string()
                        ?: throw IOException ("Empty response")

                    JSONObject(body)
                }
                if (continuation.isActive) {
                    continuation.resume(json)
                }
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }
        }
    })
}


internal fun JSONObject.optionalText(
    key: String
): String? {
    if(isNull(key)) {
        return null
    }

    return optString(key)
        .trim()
        .takeIf { it.isNotEmpty()}

}
internal fun safetyErrorMessage(
    error: Throwable
): String = when (error) {
    is SafetyHttpException ->
        "Service request failed (HTTP ${error.statusCode})."
    is IllegalArgumentException ->
        "Check the selected location and API configuration."
    is IOException ->
        "Unable to load service data. Check your connection and retry."
    else ->
        "Unable to read the service response"
}