package com.cos229239.team02.oto.ui.features

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cos229239.team02.oto.BuildConfig
import com.cos229239.team02.oto.data.resource.NpsAlertClient
import com.cos229239.team02.oto.data.resource.NwsAlertClient
import com.cos229239.team02.oto.data.resource.OpenMeteoClient
import com.cos229239.team02.oto.data.resource.OverpassResourceRepository
import com.cos229239.team02.oto.data.safety.AreaSafetyRepo
import com.cos229239.team02.oto.data.safety.DefaultAreaSafetyRepo
import com.cos229239.team02.oto.data.safety.createSafetyHttpClient

class AreaSafetyViewFactory(
    context: Context
) : ViewModelProvider.Factory {
    private val applicationContext =
        context.applicationContext
    private val areaSafetyRepo: AreaSafetyRepo by lazy {
        val http = createSafetyHttpClient()

        DefaultAreaSafetyRepo(
            resourceRepository = OverpassResourceRepository(
                context = applicationContext
            ),

            nwsClient = NwsAlertClient(
                http = http,
                userAgent = BuildConfig.NWS_USER_AGENT
            ),

            npsClient = NpsAlertClient(
                http = http,
                apiKey = BuildConfig.NPS_API_KEY
            ),
            forecastClient = OpenMeteoClient(
                httpClient = http
            )

        )
    }

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        require(modelClass == AreaSafetyView::class.java) {
            "Unknown ViewModel class: ${modelClass.name}"
        }

        @Suppress("UNCHECKED_CAST")
        return AreaSafetyView(
            repo = areaSafetyRepo
        ) as T
    }
}

