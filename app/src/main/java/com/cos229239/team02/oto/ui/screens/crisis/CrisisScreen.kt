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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cos229239.team02.oto.ui.components.OtoTopAppBar
import com.cos229239.team02.oto.ui.theme.OtoCrisisRed
import com.cos229239.team02.oto.ui.theme.OtoCrisisRedContainer
import com.cos229239.team02.oto.ui.theme.OtoHomeCrisisAction
import com.cos229239.team02.oto.ui.theme.OtoHomeCrisisActionText
import com.cos229239.team02.oto.ui.theme.OtoSpacing
import com.cos229239.team02.oto.ui.theme.OtoTextOnDark
import com.cos229239.team02.oto.ui.theme.OtoTextPrimary
import com.cos229239.team02.oto.ui.theme.OtoTextSecondary

/*
 * ---------------------------------------------------------
 * CRISIS MODE
 * ---------------------------------------------------------
 *
 * Emergency Help is shown as the primary hero card so the
 * most urgent action is immediately visible.
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
    onOfflineMapBacktrackClick: () -> Unit,
    onBackClick: () -> Unit
) {

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .verticalScroll(
                    rememberScrollState()
                )
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
             * EMERGENCY HELP HERO
             * -------------------------------------------------
             */

            CrisisHeroCard(
                title = "EMERGENCY HELP",
                description = "Get help, find critical resources, and access survival tools.",
                icon = Icons.Filled.Warning,
                buttonLabel = "OPEN EMERGENCY HELP",
                onClick = onEmergencyHelpClick
            )

            /*
             * -------------------------------------------------
             * CRISIS TOOLS
             * -------------------------------------------------
             */

            Text(
                text = "CRISIS TOOLS",

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

                CrisisActionTile(
                    title = "Offline Maps & Backtrack",
                    description = "Track your route without a signal",
                    icon = Icons.Filled.Map,
                    modifier = Modifier.weight(1f),
                    onClick = onOfflineMapBacktrackClick
                )
            }
        }
    }
}


/*
 * ---------------------------------------------------------
 * EMERGENCY HELP HERO
 * ---------------------------------------------------------
 *
 * Uses the same full-width colored card layout as the
 * Home screen, with a decorative beacon in the corner.
 */

@Composable
private fun CrisisHeroCard(
    title: String,
    description: String,
    icon: ImageVector,
    buttonLabel: String,
    onClick: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

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
                            width = 125.dp,
                            height = 100.dp
                        )
            )

            Column(
                modifier =
                    Modifier.padding(
                        OtoSpacing.Large
                    )
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    //Icon badge.
                    Box(
                        modifier =
                            Modifier
                                .size(56.dp)
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
                                Modifier.size(30.dp)
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
                                    .headlineMedium,
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
                                    .bodyMedium,
                            color =
                                Color.White.copy(
                                    alpha = 0.90f
                                )
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            OtoSpacing.Standard
                        )
                )

                //Full-width action with arrow.
                Button(
                    onClick = onClick,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min =
                                    OtoSpacing.CrisisTouchTarget
                            ),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                OtoHomeCrisisAction,
                            contentColor =
                                OtoHomeCrisisActionText
                        )
                ) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.Center
                    ) {

                        Text(
                            text = buttonLabel,
                            style =
                                MaterialTheme
                                    .typography
                                    .labelLarge
                        )

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
                            modifier =
                                Modifier.size(24.dp)
                        )
                    }
                }
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