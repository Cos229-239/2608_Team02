package com.cos229239.team02.oto.ui.screens.explorer

import android.R
import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import com.cos229239.team02.oto.navigation.OtoRoute
import com.cos229239.team02.oto.ui.theme.OtoExplorerGreen

@Composable
fun ExplorerScreen(
    onAreaSafetyClick: () -> Unit,
    onBackClick: () -> Unit,
    onPlanTripClick: () -> Unit = {},
    onCreateRouteClick: () -> Unit = {},
    onOfflineMapsClick: () -> Unit = {},

) {
    Scaffold(
        containerColor = ExplorerBackground,
        topBar = {
            ExplorerHeader(
                onMenuClick = onBackClick
            )
        },
        bottomBar = {
            ExplorerBottomBar (
                onHomeClick = onBackClick
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
                        imageVector = Icons.Default.Terrain,
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
                    style = MaterialTheme.typography.bodySmall,
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
            .background(MapBackground)
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
                )
            }
        }
    }
}
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
