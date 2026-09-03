package com.cos229239.team02.oto.ui.screens.crisis

import android.Manifest
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.heightIn

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
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
import kotlinx.coroutines.launch
import java.util.Locale
import android.content.Intent
import android.net.Uri



@Composable
fun EmergencyHelpScreen(
        //Open OTO's existing Share Status & Location screen.
        onShareStatusLocationClick: () -> Unit,

        onBackClick: () -> Unit //Return to Crisis Mode.
    ) {
    //Get the current Android screen/app context so we can check permissions.
    val context = LocalContext.current

    //Create a co routine scope so location work can run without freezing.
    val scope = rememberCoroutineScope()

    //Reuse OTO's existing Android location system.
    val locationRepository = remember(context) {
        AndroidLocationRepository(context.applicationContext)
    }

    //Store the current location after Android successfully finds it.
    var location by remember {
        mutableStateOf<OtoLocation?>(null)
    }

    //Store the message shown inside the Emergency Location card.
    var locationText by remember {
        mutableStateOf("Location not requested")
    }

    //Store a message if Android cannot open the phone dialer.
    var dialerMessage by remember {
        mutableStateOf<String?>(null)
    }

    //Ask the existing OTO location repository for the device's current location.
    fun loadLocation() {
        scope.launch {
            locationText = "Getting location..."

            location = locationRepository.getCurrentLocation()

            locationText = location?.let {
                formatEmergencyLocation(it)
            } ?: "Location unavailable — check signal and try again"
        }
    }

    //This handles Android's location permission popup.
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            //Fine location is more precise, but coarse location is also acceptable.
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                loadLocation()
            } else {
                locationText = "Location permission denied"
            }
        }

    //Check whether location permission already exists before showing Android's popup.
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
            //Permission already exists, so get the location immediately.
            loadLocation()
        } else {
            //Permission does not exist yet, so ask the user for it.
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    //Open the Android phone dialer with 911 already entered. This does not place the call automatically.//
    fun openEmergencyDialer() {

        //ACTION_DIAL opens the phone app but still requires the user to press Call.
        val dialIntent = Intent(
            Intent.ACTION_DIAL,
            Uri.parse("tel:911")
        )

        //Try to open the dialer without allowing a failure to crash OTO.
        runCatching {
            context.startActivity(dialIntent)
        }.onSuccess {
            dialerMessage = null
        }.onFailure {
            dialerMessage = "Unable to open the phone dialer on this device."
        }
    }





    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    //Move the header below Android's status bar and camera cutout.
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Use a text button so Back has a larger and clearer touch target.
                TextButton(
                    onClick = onBackClick
                ) {
                    Text(text = "← Back")
                }

                Text(
                    text = "Emergency Help",
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
                text = "Need immediate assistance?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "If you have service, contact emergency services first. Your location can help you communicate where you are.",
                style = MaterialTheme.typography.bodyMedium
            )
            //Give user a clear emergency-services action without placing a call automatically.
            Button(
                onClick = ::openEmergencyDialer,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(text = "Call 911")
            }

            Text(
                text = "Opens your phone dialer with 911 entered. You must press Call in the phone app to place the call.",
                style = MaterialTheme.typography.bodySmall
            )

            //Show a helpful message instead of crashing if no dialer is available.
            dialerMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }


            //This card keeps the location information grouped together visually.
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Emergency Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = locationText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            //The first tap gets location. Later taps refresh the same information.
            Button(
                onClick = ::requestLocation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (location == null) {
                        "Get My Location"
                    } else {
                        "Refresh Location"
                    }
                )
            }
            //Reuse the existing Share Status/Location feature for emergency help.//
            Button(
                onClick = onShareStatusLocationClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Share Status & Location")
            }

            //Keep Back available as a clearly labeled action in addition to the arrow.
            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Back")
            }
        }
    }
}

//Convert the raw location numbers into text that is easier for the user to read.
private fun formatEmergencyLocation(
    location: OtoLocation
): String {

    //Accuracy is optional because Android may not always provide it.
    val accuracy = location.accuracyMeters?.let {
        "\nAccuracy: ±${String.format(Locale.US, "%.0f", it)}m"
    } ?: ""

    return "Latitude: ${String.format(Locale.US, "%.6f", location.latitude)}" +
            "\nLongitude: ${String.format(Locale.US, "%.6f", location.longitude)}" +
            accuracy
}