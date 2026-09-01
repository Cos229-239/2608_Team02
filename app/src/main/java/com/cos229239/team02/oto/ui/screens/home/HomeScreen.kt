package com.cos229239.team02.oto.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cos229239.team02.oto.data.location.AndroidLocationRepository
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onExplorerClick: () -> Unit,
    onCrisisClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationRepository = remember(context) {
        AndroidLocationRepository(context.applicationContext)
    }

    var locationText by remember {
        mutableStateOf("Location not requested")
    }

    fun loadLocation() {
        scope.launch {
            locationText = "Getting location..."

            val location = locationRepository.getCurrentLocation()

            locationText = if (location != null) {
                "Latitude: ${location.latitude}\nLongitude: ${location.longitude}"
            } else {
                "Location unavailable"
            }
        }
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val coarseGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {
                loadLocation()
            } else {
                locationText = "Location permission denied"
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Out in the Open")
        Text(text = "Explore farther. Return safer.")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
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
        ) {
            Text(text = "Test Location")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = locationText)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onExplorerClick) {
            Text(text = "Explorer Mode")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onCrisisClick) {
            Text(text = "Crisis Mode")
        }
    }
}