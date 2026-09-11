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
import androidx.compose.foundation.layout.statusBarsPadding
//Added for Home Screen background. 1-8
import androidx.compose.foundation.Image //1
import androidx.compose.foundation.layout.Box //2
import androidx.compose.ui.layout.ContentScale //3
import androidx.compose.ui.res.painterResource //4
import androidx.compose.foundation.background //5
import androidx.compose.foundation.isSystemInDarkTheme //6
import androidx.compose.ui.graphics.Color //7
import com.cos229239.team02.oto.R //8
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import com.cos229239.team02.oto.ui.theme.OtoSpacing

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.KeyboardArrowRight

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke





@Composable
fun HomeScreen(
    onExplorerClick: () -> Unit,
    onCrisisClick: () -> Unit,
    onOfflineToolsClick: () -> Unit
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

    //Layers the wilderness artwork behind the Home screen content.
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        //Show the wilderness scenery across the full Home screen.
        Image(
            painter = painterResource(
                id = R.drawable.home_wilderness_bg
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

         //Place a light theme-aware layer over the scenery so text stays readable.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isSystemInDarkTheme()) {
                        Color.Black.copy(
                            alpha = 0.30f
                        )
                    } else {
                        Color.White.copy(
                            alpha = 0.38f
                        )
                    }
                )
        )
        //Display the Home screen content above the fixed background.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()

                //Allow Home content to scroll on smaller screens or with larger text.
                .verticalScroll(
                    rememberScrollState()
                )

                .padding(
                    horizontal = OtoSpacing.ScreenHorizontal,
                    vertical = OtoSpacing.ScreenVertical
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Show the OTO mountain mark above the app name.
                OtoMountainMark()

                Spacer(
                    modifier = Modifier.height(
                        OtoSpacing.XSmall
                    )
                )

                Text(
                    text = "OUT IN THE OPEN",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(
                    modifier = Modifier.height(
                        OtoSpacing.Small
                    )
                )

                Text(
                    text = "Explore farther. Return safer.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(
                modifier = Modifier.height(
                    OtoSpacing.SectionGap
                )
            )

            //Show location information using the Home screen card layout.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF06452F).copy(
                        alpha = 0.94f
                    )
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(
                        alpha = 0.22f
                    )
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        OtoSpacing.Large
                    )
                ) {

                    //Keep the icon and location text together.
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        //Show the location icon inside a larger circular badge.
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = Color.White.copy(
                                        alpha = 0.14f
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(
                                    30.dp
                                )
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(
                                OtoSpacing.Medium
                            )
                        )

                        //Keep the title and location information aligned together.
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "LOCATION STATUS",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )

                            Spacer(
                                modifier = Modifier.height(
                                    OtoSpacing.XSmall
                                )
                            )

                            Text(
                                text = locationText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(
                                    alpha = 0.90f
                                )
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(
                            OtoSpacing.Standard
                        )
                    )

                    //Request or refresh the device location.
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
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min = 56.dp
                            ),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF69C95A),
                            contentColor = Color.Black
                        )
                    ) {
                        //Show the action label and arrow together.
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Text(
                                text = "UPDATE LOCATION",
                                style = MaterialTheme.typography.labelLarge
                            )

                            Spacer(
                                modifier = Modifier.width(
                                    OtoSpacing.Small
                                )
                            )

                            //Show that this button opens or performs an action.
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(
                                    24.dp
                                )
                            )
                        }
                    }
                }
            }

            //Keep consistent space between Home screen cards.
            Spacer(
                modifier = Modifier.height(
                    OtoSpacing.CardGap
                )
            )

            //Show preparedness tools using the Home screen card layout.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF70424F).copy(
                        alpha = 0.94f
                    )
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(
                        alpha = 0.22f
                    )
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        OtoSpacing.Large
                    )
                ) {

                    //Keep the icon and preparedness text together.
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        //Show the preparedness icon inside a larger circular badge.
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = Color.White.copy(
                                        alpha = 0.14f
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Map,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(
                                    30.dp
                                )
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(
                                OtoSpacing.Medium
                            )
                        )

                        //Keep the title and description aligned together.
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "PREPAREDNESS",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )

                            Spacer(
                                modifier = Modifier.height(
                                    OtoSpacing.XSmall
                                )
                            )

                            Text(
                                text = "Download offline maps and review emergency resources before heading out.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(
                                    alpha = 0.90f
                                )
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(
                            OtoSpacing.Standard
                        )
                    )

                    //Open the existing Offline Maps & Backtrack tools.
                    Button(
                        onClick = onOfflineToolsClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min = 56.dp
                            ),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD59AAA),
                            contentColor = Color(0xFF4A1824)
                        )
                    ) {

                        //Show the action label and arrow together.
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Text(
                                text = "REVIEW OFFLINE TOOLS",
                                style = MaterialTheme.typography.labelLarge
                            )

                            Spacer(
                                modifier = Modifier.width(
                                    OtoSpacing.Small
                                )
                            )

                            //Show that this button opens another screen.
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(
                                    24.dp
                                )
                            )
                        }
                    }
                }
            }

            //Keep space between the preparedness reminder and Explorer Mode.
            Spacer(
                modifier = Modifier.height(
                    OtoSpacing.CardGap
                )
            )

            //Show Explorer Mode using the Home screen card layout.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF06452F).copy(
                        alpha = 0.94f
                    )
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(
                        alpha = 0.22f
                    )
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        OtoSpacing.Large
                    )
                ) {

                    //Keep the icon and Explorer text together.
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        //Show the Explorer icon inside a larger circular badge.
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = Color.White.copy(
                                        alpha = 0.14f
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Explore,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(
                                    30.dp
                                )
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(
                                OtoSpacing.Medium
                            )
                        )

                        //Keep the title and description aligned together.
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "EXPLORER MODE",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )

                            Spacer(
                                modifier = Modifier.height(
                                    OtoSpacing.XSmall
                                )
                            )

                            Text(
                                text = "Plan trips, navigate, review safety conditions, and explore with confidence.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(
                                    alpha = 0.90f
                                )
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(
                            OtoSpacing.Standard
                        )
                    )

                    //Open Explorer Mode.
                    Button(
                        onClick = onExplorerClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min = 56.dp
                            ),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF69C95A),
                            contentColor = Color.Black
                        )
                    ) {
                        //Show the Explorer action label and arrow together.
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Text(
                                text = "OPEN EXPLORER",
                                style = MaterialTheme.typography.labelLarge
                            )

                            Spacer(
                                modifier = Modifier.width(
                                    OtoSpacing.Small
                                )
                            )

                            //Show that Explorer opens another screen.
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(
                                    24.dp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(
                    OtoSpacing.CardGap
                )
            )

            //Show Crisis Mode using the Home screen emergency card layout.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFB3261E).copy(
                        alpha = 0.95f
                    )
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(
                        alpha = 0.22f
                    )
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        OtoSpacing.Large
                    )
                ) {

                    //Keep the icon and Crisis Mode text together.
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        //Show the Crisis icon inside a larger circular badge.
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = Color.White.copy(
                                        alpha = 0.14f
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(
                                    30.dp
                                )
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(
                                OtoSpacing.Medium
                            )
                        )

                        //Keep the title and description aligned together.
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "CRISIS MODE",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )

                            Spacer(
                                modifier = Modifier.height(
                                    OtoSpacing.XSmall
                                )
                            )

                            Text(
                                text = "Get emergency help, find critical resources, and access survival tools.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(
                                    alpha = 0.90f
                                )
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(
                            OtoSpacing.Standard
                        )
                    )

                    //Open Crisis Mode.
                    Button(
                        onClick = onCrisisClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min = 56.dp
                            ),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF8A80),
                            contentColor = Color(0xFF5D0B08)
                        )
                    ) {
                        //Show the Crisis action label and arrow together.
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Text(
                                text = "OPEN CRISIS MODE",
                                style = MaterialTheme.typography.labelLarge
                            )

                            Spacer(
                                modifier = Modifier.width(
                                    OtoSpacing.Small
                                )
                            )

                            //Show that Crisis Mode opens another screen.
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(
                                    24.dp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

//Show the OTO mountain logo.
@Composable
private fun OtoMountainMark() {

    Icon(
        painter = painterResource(
            id = R.drawable.ic_oto_mountain
        ),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(
            width = 116.dp,
            height = 56.dp
        )
    )
}