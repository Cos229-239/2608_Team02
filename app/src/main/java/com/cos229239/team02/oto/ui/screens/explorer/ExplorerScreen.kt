package com.cos229239.team02.oto.ui.screens.explorer


import android.R
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cos229239.team02.oto.ui.theme.OtoBackground
import com.cos229239.team02.oto.ui.theme.OtoCrisisRed
import com.cos229239.team02.oto.ui.theme.OtoExplorerGreen
import com.cos229239.team02.oto.ui.theme.OtoExplorerGreenContainer
import com.cos229239.team02.oto.ui.theme.OtoExplorerGreenDark
import com.cos229239.team02.oto.ui.theme.OtoForest700
import com.cos229239.team02.oto.ui.theme.OtoForest800
import com.cos229239.team02.oto.ui.theme.OtoLocationBlue
import com.cos229239.team02.oto.ui.theme.OtoSurface
import com.cos229239.team02.oto.ui.theme.OtoWarningAmber
import com.cos229239.team02.oto.ui.theme.OtoWarningContainer

@Composable
fun ExplorerScreen(
    onAreaSafetyClick: () -> Unit,
    onBackClick: () -> Unit,
    onPlanTripClick: () -> Unit = {},
    onCreateRouteClick: () -> Unit = {},
    onOfflineMapsClick: () -> Unit = {},
    onReportHazardClick: () -> Unit = {},
    onFieldReportsClick: () -> Unit = {}

) {
    Scaffold(
        containerColor = OtoBackground,
        topBar = {
            ExplorerHeader(
                onMenuClick = onBackClick
            )
        },
        bottomBar = {
            ExplorerBottomBar (
                onHomeClick = onBackClick,
                onAlertsClick = onAreaSafetyClick
            )
        }
        ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ExplorerMapPreview()
            }

            item {
                QuickActionsRow(
                    onPlanTripClick = onPlanTripClick,
                    onCreateRouteClick = onCreateRouteClick,
                    onOfflineMapsClick = onOfflineMapsClick
                )
            }

            item {
                SafetyOverviewCard(
                    onAreaSafetyClick = onAreaSafetyClick
                )
            }

            item {
                ReportHazardCard(
                    onClick = onReportHazardClick
                )
            }
            item {
                FieldReportsCard(
                    onClick = onFieldReportsClick
                    )
                }
            }
        }
    }

@Composable
private fun ExplorerHeader(
    onMenuClick: () -> Unit
){
    Surface(
        color = OtoExplorerGreen,
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(92.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(
                onClick = onMenuClick
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Menu",
                    modifier = Modifier.size(32.dp)
                )
            }
            Surface(
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ){
                    Icon(
                        imageVector = Icons.Default.Gif,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "EXPLORER MODE",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.82f)
                )
            }

            BadgedBox(
                badge = {
                    Badge {
                        Text("2")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun ExplorerMapPreview(){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .background(OtoSurface)
    ){
        Canvas(
            modifier = Modifier.fillMaxSize()
        ){
            repeat(7) { index ->
                val y = size.height * (index + 1) / 8f

                drawLine(
                    color = Color(0xFFB5C99A),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(
                        size.width,
                        y - 35f
                    ),
                    strokeWidth = 2f
                )
            }

            val routePath = Path().apply {
                moveTo(
                    size.width * 0.15f,
                    size.height * 0.90f
                )

                cubicTo(
                    size.width * 0.55f,
                    size.height * 0.72f,
                    size.width * 0.42f,
                    size.height * 0.64f,
                    size.width * 0.48f,
                    size.height * 0.48f
                )

                cubicTo(
                    size.width * 0.55f,
                    size.height * 0.28f,
                    size.width * 0.67f,
                    size.height * 0.24f,
                    size.width * 0.72f,
                    size.height * 0.06f
                )
            }

            drawPath(
                path = routePath,
                color = OtoExplorerGreen,
                style = Stroke(
                    width = 9f,
                    cap = StrokeCap.Round
                )
            )

            val userPosition = androidx.compose.ui.geometry.Offset(
                size.width * 0.45f,
                size.height * 0.61f
            )
            drawCircle(
                color = Color(0x552197F3),
                radius = 42f,
                center = userPosition
            )
            drawCircle(
                color = Color.White,
                radius = 21f,
                center = userPosition
            )
        }

        TrailInformationCard(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ){
            MapControlButton(
                icon = Icons.Default.Explore,
                description = "Compass"
            )

            MapControlButton(
                icon = Icons.Default.Layers,
                description = "Map layers"
            )

            MapControlButton(
                icon = Icons.Default.MyLocation,
                description = "Current location"
            )
        }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 38.dp),
                color = OtoExplorerGreen,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Terrain,
                    contentDescription = "TrailHead",
                    tint = Color.White,
                    modifier = Modifier.padding(9.dp)
                )
            }
        }
}
@Composable
private fun TrailInformationCard(
    modifier: Modifier = Modifier
){
    Surface(
        modifier = modifier,
        color = OtoExplorerGreen.copy(alpha = 0.96f),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column{
                    Text(
                        text = "Pine Ridge Tail",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Sequoia National Park",
                        style = MaterialTheme.typography.bodySmall
                    )

                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Elevation: 6,240 ft",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "* 72F Sunny",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
@Composable
private fun MapControlButton(
    icon: ImageVector,
    description: String
){
    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        IconButton(
            onClick = { }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = OtoExplorerGreenDark
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onPlanTripClick: () -> Unit,
    onCreateRouteClick: () -> Unit,
    onOfflineMapsClick: () -> Unit
){
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ExplorerActionCard(
                icon = Icons.Default.Map,
                title = "PLAN TRIP",
                description = "Plan or edit an existing trip",
                onClick = onPlanTripClick
            )
        }

        item {
            ExplorerActionCard(
                icon = Icons.Default.AddCircleOutline,
                title = "CREATE NEW ROUTE",
                description = "Build a custom route",
                onClick = onCreateRouteClick
            )
        }
        item {
            ExplorerActionCard(
                icon = Icons.Default.Download,
                title = "OFFLINE MAPS",
                description = "Download maps for offline use",
                onClick = onOfflineMapsClick
            )
        }
    }
}

@Composable
private fun ExplorerActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
){
    Card(
        modifier = Modifier
            .width(155.dp)
            .height(180.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OtoExplorerGreenDark,
                modifier = Modifier.size(42.dp)

            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = OtoExplorerGreenDark,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SafetyOverviewCard(
    onAreaSafetyClick: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAreaSafetyClick),
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = OtoExplorerGreenDark,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "SAFETY OVERVIEW",
                    color = OtoExplorerGreenDark,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                SafetyMetric(
                    modifier = Modifier.weight(1f),
                    title = "WEATHER",
                    icon = Icons.Default.WbSunny,
                    iconColor = Color(0xFFFFB000),
                    mainValue = "72F",
                    detail = "Sunny\n0% rain"
                )

                SafetyMetric(
                    modifier = Modifier.weight(1f),
                    title = "HAZARDS",
                    icon = Icons.Default.Warning,
                    iconColor = OtoWarningAmber,
                    mainValue = "2",
                    detail = "Reported\nnearby"
                )

                SafetyMetric(
                    modifier = Modifier.weight(1f),
                    title = "CLOSURES",
                    icon = Icons.Default.Block,
                    iconColor = OtoCrisisRed,
                    mainValue = "1",
                    detail = "Trail closure\nnearby"
                )

                SafetyMetric(
                    modifier = Modifier.weight(1f),
                    title = "AIR QUALITY",
                    icon = Icons.Default.Eco,
                    iconColor = Color(0xFF67A832),
                    mainValue = "Good",
                    detail = "AQI 28"
                )
            }
        }
    }
}

@Composable
private fun SafetyMetric(
    modifier: Modifier,
    title: String,
    icon: ImageVector,
    iconColor: Color,
    mainValue: String,
    detail: String
){
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )

        Text(
            text = mainValue,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReportHazardCard(
    onClick: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = OtoWarningContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFF6F4300),
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ){
                Text(
                    text = "REPORT HAZARD / ROUTE CHANGE",
                    color = OtoExplorerGreenDark,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Help keep the trails safe for everyone",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = ">",
                color = OtoExplorerGreenDark,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun FieldReportsCard(
    onClick: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Assignment,
                contentDescription = null,
                tint = OtoExplorerGreenDark,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "FIELD REPORTS",
                    color = OtoExplorerGreenDark,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Recent Reports in the Area",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = ">",
                color = OtoExplorerGreenDark,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun ExplorerBottomBar(
    onHomeClick: () -> Unit,
    onAlertsClick: () -> Unit
){
    NavigationBar(
        containerColor = OtoExplorerGreen
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",

                )
            },
            label = {
                Text("HOME")
            },
            colors = explorerNavigationColors()
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Explorer",

                )

            },
            label = {
                Text("EXPLORER")
            },
            colors = explorerNavigationColors()
        )
        NavigationBarItem(
            selected = false,
            onClick = onAlertsClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alerts",

                )
            },

            label = {
                Text("AREA ALERTS")
            },
            colors = explorerNavigationColors()
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",

                )
            },
            label = {
                Text("PROFILE")
            },
            colors = explorerNavigationColors()
        )
    }
}
@Composable
private fun explorerNavigationColors() =

    NavigationBarItemDefaults.colors(
        selectedIconColor = OtoLocationBlue,
        selectedTextColor = OtoForest700,
        unselectedIconColor =
            Color.White.copy(alpha = 0.75f),
        unselectedTextColor =
            Color.White.copy(alpha = 0.75f),
        indicatorColor = OtoExplorerGreenDark

    )

//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        Text(text = "Explorer Mode")
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Button(
//            onClick = { },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Plan Trip")
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        Button(
//            onClick = { },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Create New Route")
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        Button(
//            onClick = { },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Offline Maps")
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        Button(
//            onClick = onAreaSafetyClick,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Area Safety")
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        Button(
//            onClick = { },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Report Hazard")
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Button(
//            onClick = onBackClick,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Back")
//        }
//    }
//}
