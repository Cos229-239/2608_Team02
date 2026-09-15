package com.cos229239.team02.oto.ui.screens.home


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cos229239.team02.oto.R
import com.cos229239.team02.oto.data.location.AndroidLocationRepository
import com.cos229239.team02.oto.ui.theme.OtoExplorerGreen
import com.cos229239.team02.oto.ui.theme.OtoExplorerGreenContainer
import com.cos229239.team02.oto.ui.theme.OtoHomeCrisisAction
import com.cos229239.team02.oto.ui.theme.OtoHomeCrisisActionText
import com.cos229239.team02.oto.ui.theme.OtoHomeCrisisCardDark
import com.cos229239.team02.oto.ui.theme.OtoHomeCrisisCardLight
import com.cos229239.team02.oto.ui.theme.OtoHomeForestCardDark
import com.cos229239.team02.oto.ui.theme.OtoHomeForestCardLight
import com.cos229239.team02.oto.ui.theme.OtoHomePreparednessAction
import com.cos229239.team02.oto.ui.theme.OtoHomePreparednessActionText
import com.cos229239.team02.oto.ui.theme.OtoHomePreparednessCardDark
import com.cos229239.team02.oto.ui.theme.OtoHomePreparednessCardLight
import com.cos229239.team02.oto.ui.theme.OtoSpacing
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(
    onExplorerClick: () -> Unit,
    onCrisisClick: () -> Unit,
    onOfflineToolsClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    //Use calmer branding colors when the phone is in dark mode.
    val darkTheme = isSystemInDarkTheme()
    //Use shared OTO branding colors for the Home header and footer.
    val homeBrandColor =
        if (darkTheme) {
            OtoExplorerGreenContainer
        } else {
            MaterialTheme.colorScheme.primary
        }

    val homeAccentColor =
        if (darkTheme) {
            OtoExplorerGreen
        } else {
            MaterialTheme.colorScheme.primary
        }

    //Select the Home card colors for the current system theme.
    val homeForestCardColor =
        if (darkTheme) {
            OtoHomeForestCardDark
        } else {
            OtoHomeForestCardLight
        }

    val homePreparednessCardColor =
        if (darkTheme) {
            OtoHomePreparednessCardDark
        } else {
            OtoHomePreparednessCardLight
        }

    val homeCrisisCardColor =
        if (darkTheme) {
            OtoHomeCrisisCardDark
        } else {
            OtoHomeCrisisCardLight
        }

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
                    if (darkTheme) {
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
                OtoMountainMark(
                    tint = homeAccentColor
                )

                Spacer(
                    modifier = Modifier.height(
                        OtoSpacing.XSmall
                    )
                )

                Text(
                    text = "OUT IN THE OPEN",
                    style = MaterialTheme.typography.headlineLarge,
                    color = homeBrandColor
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

            //Show Location Status using the shared Home action card.
            HomeActionCard(
                title = "LOCATION STATUS",
                description = locationText,
                icon = Icons.Filled.LocationOn,
                buttonLabel = "UPDATE LOCATION",
                cardColor = homeForestCardColor.copy(
                    alpha = 0.94f
                ),
                buttonColor = OtoExplorerGreen,
                buttonContentColor = Color.Black,

                //Request permission if needed, otherwise refresh the current location.
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

                //Draw the Location topographic artwork behind the card content.
                artwork = {
                    HomeLocationArtwork()
                }
            )

            //Keep consistent space between Home screen cards.
            Spacer(
                modifier = Modifier.height(
                    OtoSpacing.CardGap
                )
            )

            //Show Preparedness using the shared Home action card.
            HomeActionCard(
                title = "PREPAREDNESS",
                description = "Download offline maps and review emergency resources before heading out.",
                icon = Icons.Filled.Map,
                buttonLabel = "REVIEW OFFLINE TOOLS",
                cardColor = homePreparednessCardColor.copy(
                    alpha = 0.94f
                ),
                buttonColor = OtoHomePreparednessAction,
                buttonContentColor = OtoHomePreparednessActionText,
                onClick = onOfflineToolsClick,

                //Draw the Preparedness tree artwork behind the card content.
                artwork = {
                    HomePreparednessArtwork()
                }
            )

            //Keep space between the preparedness reminder and Explorer Mode.
            Spacer(
                modifier = Modifier.height(
                    OtoSpacing.CardGap
                )
            )

            //Show Explorer Mode using the shared Home action card.
            HomeActionCard(
                title = "EXPLORER MODE",
                description = "Plan trips, navigate, review safety conditions, and explore with confidence.",
                icon = Icons.Filled.Explore,
                buttonLabel = "OPEN EXPLORER",
                cardColor = homeForestCardColor.copy(
                    alpha = 0.94f
                ),
                buttonColor = OtoExplorerGreen,
                buttonContentColor = Color.Black,
                onClick = onExplorerClick,

                //Draw the Explorer mountain artwork behind the card content.
                artwork = {
                    HomeExplorerArtwork()
                }
            )

            Spacer(
                modifier = Modifier.height(
                    OtoSpacing.CardGap
                )
            )

            //Show Crisis Mode using the shared Home action card.
            HomeActionCard(
                title = "CRISIS MODE",
                description = "Get emergency help, find critical resources, and access survival tools.",
                icon = Icons.Filled.Warning,
                buttonLabel = "OPEN CRISIS MODE",
                cardColor = homeCrisisCardColor.copy(
                    alpha = 0.95f
                ),
                buttonColor = OtoHomeCrisisAction,
                buttonContentColor = OtoHomeCrisisActionText,
                onClick = onCrisisClick,

                //Draw the Crisis emergency beacon artwork behind the card content.
                artwork = {
                    HomeCrisisArtwork()
                }
            )

            //Add space/room below the final Home card.
            Spacer(
                modifier = Modifier.height(
                    OtoSpacing.Large
                )
            )

            //Show a small branded footer at the bottom of Home.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                //Draw the left footer line.
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(1.dp)
                        .background(
                            homeBrandColor.copy(
                                alpha = 0.55f
                            )
                        )
                )

                Spacer(
                    modifier = Modifier.width(
                        OtoSpacing.Small
                    )
                )

                Text(
                    text = "PREPARE • EXPLORE • RETURN SAFER",
                    style = MaterialTheme.typography.labelSmall,
                    color = homeBrandColor
                )

                Spacer(
                    modifier = Modifier.width(
                        OtoSpacing.Small
                    )
                )

                //Draw the right footer line.
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(1.dp)
                        .background(
                            homeBrandColor.copy(
                                alpha = 0.55f
                            )
                        )
                )
            }

            //Keep the footer above the Android gesture area.
            Spacer(
                modifier = Modifier.height(
                    OtoSpacing.Large
                )
            )



        }
    }
}

//Show the OTO mountain logo.
@Composable
private fun OtoMountainMark(
    tint: Color
) {

    Icon(
        painter = painterResource(
            id = R.drawable.ic_oto_mountain
        ),
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(
            width = 116.dp,
            height = 56.dp
        )
    )
}