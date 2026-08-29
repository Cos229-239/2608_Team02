package com.cos229239.team02.oto.ui.screens.crisis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cos229239.team02.oto.data.location.AndroidLocationRepository
import com.cos229239.team02.oto.data.location.OtoLocation
import kotlinx.coroutines.launch
import java.util.Locale

private enum class ShareStatus(val label: String) {
    Ok("I'm OK"),
    NeedHelp("Need help"),
    Injured("Injured"),
    Lost("Lost"),
    StayingPut("Staying put"),
    CheckingIn("Just checking in")
}

@Composable
fun ShareStatusLocationScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationRepository = remember(context) {
        AndroidLocationRepository(context.applicationContext)
    }

    var location by remember { mutableStateOf<OtoLocation?>(null) }
    var locationText by remember { mutableStateOf("Location not requested") }
    var status by remember { mutableStateOf(ShareStatus.Ok) }
    var shared by remember { mutableStateOf(false) }

    fun loadLocation() {
        scope.launch {
            locationText = "Getting location..."
            location = locationRepository.getCurrentLocation()
            locationText = location?.let { formatLocation(it) }
                ?: "Location unavailable — check signal and try again"
            shared = false
        }
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                loadLocation()
            } else {
                locationText = "Location permission denied"
            }
        }

    fun requestLocation() {
        val fineGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            loadLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun shareStatus() {
        val message = buildShareMessage(status, location)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        runCatching {
            context.startActivity(
                Intent.createChooser(sendIntent, "Share status and location")
            )
        }.onSuccess {
            shared = true
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Text("\u2190", style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    text = "Share Status and Location",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Let someone know where you are and how you're doing. Your location is only sent when you share it.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = ::requestLocation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (location == null) "Get My Location" else "Refresh Location"
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = locationText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text(
                text = "Select your status:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ShareStatus.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = status == option,
                        onClick = {
                            status = option
                            shared = false
                        }
                    )
                    Text(
                        text = option.label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Button(
                onClick = ::shareStatus,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Share Status & Location")
            }

            if (shared) {
                Text(
                    text = "Location shared. Stay safe.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun formatLocation(location: OtoLocation): String {
    val accuracy = location.accuracyMeters?.let {
        "\nAccuracy: ±${String.format(Locale.US, "%.0f", it)}m"
    } ?: ""

    return "Latitude: ${String.format(Locale.US, "%.6f", location.latitude)}" +
        "\nLongitude: ${String.format(Locale.US, "%.6f", location.longitude)}" +
        accuracy
}

private fun buildShareMessage(
    status: ShareStatus,
    location: OtoLocation?
): String {
    val locationLine = if (location != null) {
        val mapUrl =
            "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        "Location: $mapUrl\n" +
            "Coordinates: ${String.format(Locale.US, "%.6f", location.latitude)}, " +
                String.format(Locale.US, "%.6f", location.longitude) +
            (location.accuracyMeters?.let {
                " (±${String.format(Locale.US, "%.0f", it)}m accuracy)"
            } ?: "")
    } else {
        "Location unavailable"
    }

    return "OTO check-in\nStatus: ${status.label}\n$locationLine"
}