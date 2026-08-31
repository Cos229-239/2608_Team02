package com.cos229239.team02.oto.ui.screens.crisis

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.cos229239.team02.oto.data.resource.NearbyResource
import com.cos229239.team02.oto.data.resource.OverpassResourceRepository
import com.cos229239.team02.oto.data.resource.ResourceResult
import com.cos229239.team02.oto.data.resource.ResourceType
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun NearbyResourceScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationRepository = remember(context) {
        AndroidLocationRepository(context.applicationContext)
    }

    val resourceRepository = remember(context) {
        OverpassResourceRepository(context.applicationContext)
    }

    var location by remember { mutableStateOf<OtoLocation?>(null) }
    var statusText by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<ResourceResult?>(null) }
    var loading by remember { mutableStateOf(false) }
    var activeFilters by remember {
        mutableStateOf(
            setOf(
                ResourceType.Medical,
                ResourceType.Emergency,
                ResourceType.Water,
                ResourceType.Food
            )
        )
    }
    var showUnnamed by remember { mutableStateOf(false) }

    fun loadResources(target: OtoLocation, forceRefresh: Boolean = false) {
        scope.launch {
            loading = true
            statusText = if (forceRefresh) "Refreshing resources..." else "Finding resources near you..."
            val loaded = resourceRepository.getNearby(target, forceRefresh = forceRefresh)
            location = target
            result = loaded
            loading = false
            statusText = loaded.error ?: if (loaded.fromCache) {
                "Showing saved results. Connect to refresh."
            } else {
                "Showing resources near you."
            }
        }
    }

    fun loadExpanded(target: OtoLocation) {
        scope.launch {
            loading = true
            statusText = "Expanding search..."
            val loaded = resourceRepository.getNearby(
                target,
                OverpassResourceRepository.EXPANDED_RADIUS_METERS
            )
            location = target
            result = loaded
            loading = false
            statusText = loaded.error ?: if (loaded.fromCache) {
                "Showing saved results. Connect to refresh."
            } else {
                "Showing expanded results (${formatKm(loaded.radiusMeters)})."
            }
        }
    }

    fun loadLocation(forceRefresh: Boolean = false) {
        scope.launch {
            loading = true
            statusText = "Finding your location..."
            val current = locationRepository.getCurrentLocation()
            loading = false
            if (current != null) {
                loadResources(current, forceRefresh)
            } else {
                statusText = "Location unavailable — check signal and try again"
            }
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
                statusText = "Location permission denied"
            }
        }

    fun requestLocation(forceRefresh: Boolean = false) {
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
            loadLocation(forceRefresh)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
                    text = "Nearby Resources",
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
                text = "Find emergency resources near you using OpenStreetMap. Results are cached so you can view them offline.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = { requestLocation(forceRefresh = result != null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (result == null) "Find Nearby Resources" else "Refresh Resources"
                )
            }

            if (loading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                }
            }

            if (location != null) {
                Text(
                    text = formatLocation(location!!),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium
            )

            val resources = result?.resources ?: emptyList()
            val currentRadius = result?.radiusMeters

            if (currentRadius != null &&
                currentRadius < OverpassResourceRepository.EXPANDED_RADIUS_METERS
            ) {
                Button(
                    onClick = { location?.let(::loadExpanded) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Expand search to ${formatKm(OverpassResourceRepository.EXPANDED_RADIUS_METERS)}"
                    )
                }
            }

            if (resources.isNotEmpty() &&
                currentRadius != null &&
                currentRadius >= OverpassResourceRepository.EXPANDED_RADIUS_METERS
            ) {
                Text(
                    text = "Showing results from an expanded search (${formatKm(currentRadius)})",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (result != null && result?.error == null && resources.isEmpty()) {
                Text(
                    text = "No resources found within range of your location. Use Expand search for wider coverage.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (resources.isNotEmpty()) {
                FilterRow(
                    activeFilters = activeFilters,
                    showUnnamed = showUnnamed,
                    onToggle = { type ->
                        activeFilters = if (type in activeFilters) {
                            activeFilters - type
                        } else {
                            activeFilters + type
                        }
                    },
                    onToggleUnnamed = { showUnnamed = !showUnnamed }
                )

                val filtered = resources.filter { resource ->
                    (showUnnamed || !resource.name.startsWith("Unnamed")) &&
                        resource.types.any { it in activeFilters }
                }

                if (filtered.isEmpty()) {
                    Text(
                        text = "No resources match the selected filters.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    filtered.forEach { resource ->
                        ResourceCard(resource = resource)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    activeFilters: Set<ResourceType>,
    showUnnamed: Boolean,
    onToggle: (ResourceType) -> Unit,
    onToggleUnnamed: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ResourceType.entries.forEach { type ->
            FilterChip(
                selected = type in activeFilters,
                onClick = { onToggle(type) },
                label = { Text(text = type.label) }
            )
        }
        FilterChip(
            selected = showUnnamed,
            onClick = onToggleUnnamed,
            label = { Text(text = "Unnamed") }
        )
    }
}

@Composable
private fun ResourceCard(resource: NearbyResource) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = resource.types.joinToString(", ") { it.label },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "${formatDistance(resource.distanceKm)} away",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatDistance(km: Double): String {
    val rounded = (km * 10).roundToInt() / 10.0
    return String.format(Locale.US, "%.1f km", rounded)
}

private fun formatKm(meters: Int): String {
    val km = meters / 1000.0
    return String.format(Locale.US, "%.0f km", km)
}

private fun formatLocation(location: OtoLocation): String {
    val accuracy = location.accuracyMeters?.let {
        "\nAccuracy: ±${String.format(Locale.US, "%.0f", it)}m"
    } ?: ""

    return "Latitude: ${String.format(Locale.US, "%.6f", location.latitude)}" +
        "\nLongitude: ${String.format(Locale.US, "%.6f", location.longitude)}" +
        accuracy
}
