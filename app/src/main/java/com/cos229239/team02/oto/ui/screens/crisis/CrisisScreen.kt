package com.cos229239.team02.oto.ui.screens.crisis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cos229239.team02.oto.ui.components.OtoTopAppBar
import com.cos229239.team02.oto.ui.features.OfflineMapBacktrackViewModel
import com.cos229239.team02.oto.ui.theme.OtoCrisisRed
import com.cos229239.team02.oto.ui.theme.OtoCrisisRedContainer
import com.cos229239.team02.oto.ui.theme.OtoSpacing
import com.cos229239.team02.oto.ui.theme.OtoTextOnDark
import com.cos229239.team02.oto.ui.theme.OtoTextPrimary
import com.cos229239.team02.oto.ui.theme.OtoTextSecondary

/*
 * ---------------------------------------------------------
 * CRISIS MODE
 * ---------------------------------------------------------
 *
 * Emergency Help is shown as a compact hero card so the
 * most urgent action is immediately visible.
 *
 * The hero and live tracking map stay pinned above the
 * scroll so map gestures don't fight the dashboard, and
 * route tracking, backtracking, and the remaining tools
 * scroll beneath them.
 *
 * On short landscape screens (phones in landscape) the map
 * and dashboard split side by side so both stay reachable.
 *
 * The remaining tools use the same tile layout as the
 * Explorer dashboard (white cards, icon badge, title,
 * description) with the Crisis red color identity.
 */

@Composable
fun CrisisScreen(
    onEmergencyHelpClick: () -> Unit,
    onFirstAidSurvivalClick: () -> Unit,
    onShareStatusLocationClick: () -> Unit,
    onNearbyResourcesClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: OfflineMapBacktrackViewModel =
        viewModel()
) {

    val requestLocation =
        rememberOfflineMapBacktrackLocation(
            viewModel
        )

    Scaffold(
        topBar = {

            //Use OTO's shared Material 3 top app bar.
            OtoTopAppBar(
                title = "CRISIS MODE",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->

        val backgroundColor =
            if (isSystemInDarkTheme()) {
                //Match the theme's dark surface so the screen
                //feels genuinely dark in dark mode.
                Color(0xFF111A14)
            } else {
                //Light content area, matching Explorer.
                Color(0xFFF7F8F6)
            }

        val configuration =
            LocalConfiguration.current

        val isLandscape =
            configuration.screenWidthDp >
                configuration.screenHeightDp

        // Short landscape screens (phones in landscape) cannot fit
        // the pinned hero + map and a usable dashboard stacked, so
        // they use the side-by-side pane layout instead.
        val shortLandscape =
            isLandscape &&
                configuration.screenHeightDp < 520

        val trackingMapHeight =
            if (isLandscape) {
                200.dp
            } else {
                300.dp
            }

        if (shortLandscape) {

            CrisisLandscapePane(
                paddingValues = paddingValues,
                backgroundColor = backgroundColor,
                viewModel = viewModel,
                requestLocation = requestLocation,
                onEmergencyHelpClick = onEmergencyHelpClick,
                onFirstAidSurvivalClick = onFirstAidSurvivalClick,
                onShareStatusLocationClick = onShareStatusLocationClick,
                onNearbyResourcesClick = onNearbyResourcesClick
            )

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues)
                    .padding(
                        horizontal = OtoSpacing.ScreenHorizontal,
                        vertical = OtoSpacing.ScreenVertical
                    ),
                verticalArrangement = Arrangement.spacedBy(
                    OtoSpacing.Medium
                )
            ) {

                /*
                 * -------------------------------------------------
                 * FIXED: EMERGENCY HELP HERO + TRACKING MAP
                 * -------------------------------------------------
                 *
                 * The hero and live map stay pinned above the scroll
                 * so pan/zoom gestures on the map never fight the
                 * dashboard's scrolling.
                 */

                CrisisHeroCard(
                    title = "EMERGENCY HELP",
                    description = "Get help, find critical resources, and access survival tools.",
                    icon = Icons.Filled.Warning,
                    onClick = onEmergencyHelpClick
                )

                SectionHeader(text = "TRACK & NAVIGATION")

                Text(
                    text = "Track your route and find your way back, even without a signal.",

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        if (isSystemInDarkTheme()) {
                            OtoTextOnDark
                        } else {
                            OtoTextSecondary
                        }
                )

                LiveTrackingMapCard(
                    viewModel = viewModel,
                    onLocateMeClick = requestLocation,
                    modifier =
                        Modifier.fillMaxWidth(),
                    mapHeight = trackingMapHeight
                )

                /*
                 * -------------------------------------------------
                 * SCROLLABLE DASHBOARD
                 * -------------------------------------------------
                 *
                 * Route tracking, backtracking, and the crisis tool
                 * tiles scroll beneath the pinned hero and map.
                 */

                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(
                                rememberScrollState()
                            ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            OtoSpacing.Medium
                        )
                ) {

                    TrackingCards(
                        viewModel = viewModel
                    )

                    CrisisToolsSection(
                        onFirstAidSurvivalClick = onFirstAidSurvivalClick,
                        onShareStatusLocationClick = onShareStatusLocationClick,
                        onNearbyResourcesClick = onNearbyResourcesClick
                    )
                }
            }
        }
    }
}


/*
 * ---------------------------------------------------------
 * LANDSCAPE PANE
 * ---------------------------------------------------------
 *
 * Used on short landscape screens (phones in landscape) where
 * the stacked layout leaves no room for the scrollable
 * dashboard.
 *
 * The live tracking map is pinned full height on the left,
 * out of any scroll so map gestures never fight it, and the
 * emergency hero and whole dashboard sit in the right half.
 */

@Composable
private fun CrisisLandscapePane(
    paddingValues: PaddingValues,
    backgroundColor: Color,
    viewModel: OfflineMapBacktrackViewModel,
    requestLocation: () -> Unit,
    onEmergencyHelpClick: () -> Unit,
    onFirstAidSurvivalClick: () -> Unit,
    onShareStatusLocationClick: () -> Unit,
    onNearbyResourcesClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(paddingValues)
            .padding(
                horizontal = OtoSpacing.ScreenHorizontal,
                vertical = OtoSpacing.ScreenVertical
            ),
        horizontalArrangement = Arrangement.spacedBy(
            OtoSpacing.Medium
        )
    ) {

        //Left = live tracking map pinned full height.
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            verticalArrangement =
                Arrangement.spacedBy(
                    OtoSpacing.Medium
                )
        ) {

            SectionHeader(text = "TRACK & NAVIGATION")

            LiveTrackingMapCard(
                viewModel = viewModel,
                onLocateMeClick = requestLocation,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                matchParentHeight = true
            )
        }

        //Right = emergency hero pinned above the scrollable dashboard.
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            verticalArrangement =
                Arrangement.spacedBy(
                    OtoSpacing.Medium
                )
        ) {

            CrisisHeroCard(
                title = "EMERGENCY HELP",
                description = "Get help, find critical resources, and access survival tools.",
                icon = Icons.Filled.Warning,
                onClick = onEmergencyHelpClick
            )

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(
                            rememberScrollState()
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        OtoSpacing.Medium
                    )
            ) {

                TrackingCards(
                    viewModel = viewModel
                )

                CrisisToolsSection(
                    onFirstAidSurvivalClick = onFirstAidSurvivalClick,
                    onShareStatusLocationClick = onShareStatusLocationClick,
                    onNearbyResourcesClick = onNearbyResourcesClick
                )
            }
        }
    }
}

/*
 * ---------------------------------------------------------
 * TRACKING CARDS
 * ---------------------------------------------------------
 *
 * The route tracking and backtrack cards, emitted as siblings
 * so the parent's spaced-by arrangement applies between them.
 */

@Composable
private fun TrackingCards(
    viewModel: OfflineMapBacktrackViewModel
) {

    RouteTrackingCard(
        viewModel = viewModel
    )

    BacktrackCard(
        viewModel = viewModel
    )
}

/*
 * ---------------------------------------------------------
 * CRISIS TOOLS SECTION
 * ---------------------------------------------------------
 */

@Composable
private fun CrisisToolsSection(
    onFirstAidSurvivalClick: () -> Unit,
    onShareStatusLocationClick: () -> Unit,
    onNearbyResourcesClick: () -> Unit
) {

    SectionHeader(text = "CRISIS TOOLS")

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                OtoSpacing.Medium
            )
    ) {

        CrisisActionTile(
            title = "First Aid & Survival",
            description = "Emergency and outdoor safety information",
            icon = Icons.Filled.Favorite,
            modifier = Modifier.weight(1f),
            onClick = onFirstAidSurvivalClick
        )

        CrisisActionTile(
            title = "Share Status & Location",
            description = "Let trusted contacts know where you are",
            icon = Icons.Filled.Share,
            modifier = Modifier.weight(1f),
            onClick = onShareStatusLocationClick
        )
    }

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                OtoSpacing.Medium
            )
    ) {

        CrisisActionTile(
            title = "Nearby Resources",
            description = "Critical services in your area",
            icon = Icons.Filled.Place,
            modifier = Modifier.weight(1f),
            onClick = onNearbyResourcesClick
        )
    }
}

/*
 * ---------------------------------------------------------
 * SECTION HEADER
 * ---------------------------------------------------------
 */

@Composable
private fun SectionHeader(
    text: String
) {

    Text(
        text = text,

        fontSize =
            11.sp,

        color =
            if (isSystemInDarkTheme()) {
                OtoTextOnDark
            } else {
                OtoTextSecondary
            },

        fontWeight =
            FontWeight.Bold
    )
}


/*
 * ---------------------------------------------------------
 * EMERGENCY HELP HERO
 * ---------------------------------------------------------
 *
 * A compact full-width red emergency action. The whole card
 * is the button, so it stays one row and leaves room for the
 * pinned tracking map above the dashboard scroll.
 */

@Composable
private fun CrisisHeroCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min =
                        OtoSpacing.CrisisTouchTarget
                ),

        shape =
            MaterialTheme.shapes.large,

        colors =
            CardDefaults.cardColors(
                containerColor =
                    OtoCrisisRed
            ),

        border =
            BorderStroke(
                width = 1.dp,
                color =
                    Color.White.copy(
                        alpha = 0.22f
                    )
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
    ) {

        Box(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            //Draw a faint emergency beacon in the corner.
            CrisisBeaconArtwork(
                modifier =
                    Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .size(
                            width = 88.dp,
                            height = 64.dp
                        )
            )

            Row(
                modifier =
                    Modifier.padding(
                        OtoSpacing.Medium
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                //Icon badge.
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .background(
                                color =
                                    Color.White.copy(
                                        alpha = 0.14f
                                    ),
                                shape = CircleShape
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier =
                            Modifier.size(24.dp)
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            OtoSpacing.Medium
                        )
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text = title,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                OtoSpacing.XSmall
                            )
                    )

                    Text(
                        text = description,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            Color.White.copy(
                                alpha = 0.90f
                            )
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            OtoSpacing.Small
                        )
                )

                Icon(
                    imageVector =
                        Icons.AutoMirrored
                            .Filled
                            .KeyboardArrowRight,
                    contentDescription = null,
                    tint =
                        Color.White.copy(
                            alpha = 0.90f
                        ),
                    modifier =
                        Modifier.size(24.dp)
                )
            }
        }
    }
}


/*
 * ---------------------------------------------------------
 * CRISIS ACTION TILE
 * ---------------------------------------------------------
 *
 * Matches the Explorer dashboard quick-action tiles, using
 * the Crisis red identity for the icon badge.
 *
 * Text colors are pinned so tiles stay readable on the
 * white background in light and dark mode.
 */

@Composable
private fun CrisisActionTile(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier =
            modifier.heightIn(min = 150.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        shape =
            RoundedCornerShape(14.dp)
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        OtoSpacing.Medium
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {

            //Icon badge with the Crisis color identity.
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .background(
                            color =
                                OtoCrisisRedContainer,
                            shape = CircleShape
                        ),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OtoCrisisRed,
                    modifier =
                        Modifier.size(24.dp)
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        OtoSpacing.Small
                    )
            )

            Text(
                text = title,
                color =
                    OtoTextPrimary,
                fontWeight =
                    FontWeight.Bold,
                fontSize = 13.sp,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(
                        OtoSpacing.XSmall
                    )
            )

            Text(
                text = description,
                color =
                    OtoTextSecondary,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                textAlign =
                    TextAlign.Center
            )
        }
    }
}


/*
 * ---------------------------------------------------------
 * EMERGENCY BEACON ARTWORK
 * ---------------------------------------------------------
 *
 * Faint beacon with radiating rays in the upper-right
 * corner of the hero card.
 */

@Composable
private fun CrisisBeaconArtwork(
    modifier: Modifier = Modifier
) {

    val artworkColor =
        Color.White.copy(
            alpha = 0.08f
        )

    Canvas(
        modifier =
            modifier
    ) {

        val centerX =
            size.width * 0.72f

        val centerY =
            size.height * 0.38f

        //Outer beacon ring.
        drawCircle(
            color = artworkColor,
            radius = 34.dp.toPx(),
            center =
                androidx.compose.ui.geometry.Offset(
                    centerX,
                    centerY
                ),
            style =
                Stroke(
                    width = 1.5.dp.toPx()
                )
        )

        //Inner beacon ring.
        drawCircle(
            color = artworkColor,
            radius = 20.dp.toPx(),
            center =
                androidx.compose.ui.geometry.Offset(
                    centerX,
                    centerY
                ),
            style =
                Stroke(
                    width = 1.5.dp.toPx()
                )
        )

        //Radiating rays.
        val ray = Path().apply {
            moveTo(centerX, centerY - 46.dp.toPx())
            lineTo(centerX, centerY - 38.dp.toPx())

            moveTo(centerX, centerY + 46.dp.toPx())
            lineTo(centerX, centerY + 38.dp.toPx())

            moveTo(centerX - 46.dp.toPx(), centerY)
            lineTo(centerX - 38.dp.toPx(), centerY)

            moveTo(centerX + 46.dp.toPx(), centerY)
            lineTo(centerX + 38.dp.toPx(), centerY)
        }

        drawPath(
            path = ray,
            color = artworkColor,
            style =
                Stroke(
                    width = 1.5.dp.toPx()
                )
        )
    }
}