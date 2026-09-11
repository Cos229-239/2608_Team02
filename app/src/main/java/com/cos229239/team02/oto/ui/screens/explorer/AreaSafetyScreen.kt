package com.cos229239.team02.oto.ui.screens.explorer

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cos229239.team02.oto.BuildConfig
import com.cos229239.team02.oto.data.resource.NpsAlertClient
import com.cos229239.team02.oto.data.resource.NpsParkPicker
import com.cos229239.team02.oto.ui.features.AreaSafetyUIState
import com.cos229239.team02.oto.ui.features.AreaSafetyView
import com.cos229239.team02.oto.data.safety.SafetyFilter
import com.cos229239.team02.oto.data.safety.SafetyLevel
import com.cos229239.team02.oto.data.safety.SafetyNotification
import com.cos229239.team02.oto.data.safety.createSafetyHttpClient
import com.cos229239.team02.oto.ui.components.OtoTopAppBar //Use OTO's shared Material 3 top app bar.
import com.cos229239.team02.oto.ui.features.WeatherForecastCard
import androidx.compose.foundation.layout.PaddingValues

//Connects ViewModel's state and actions to the Area Safety screen.
@Composable
fun AreaSafetyRoute( onBackClick: () -> Unit,
                     safetyView: AreaSafetyView
)
{

    val uiState by safetyView.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    val parkClient = remember {
        NpsAlertClient(
            http = createSafetyHttpClient(),
            apiKey = BuildConfig.NPS_API_KEY
        )
    }

    AreaSafetyScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRefresh = safetyView::refreshNotifications,
        onFilterSelected = safetyView::selectFilter,
        onSourceClick = { sourceUrl ->
            uriHandler.openUri(sourceUrl)
        },
        parkCodeContent = {
            NpsParkPicker(
                client = parkClient,
                selectedParkCode = uiState.selectedParkCode,
                onParkSelected = safetyView::selectParkCode
            )
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaSafetyScreen(
    uiState: AreaSafetyUIState,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onFilterSelected: (SafetyFilter) -> Unit,
    onSourceClick: (String) -> Unit,
    parkCodeContent: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            OtoTopAppBar(
                title = "AREA ALERTS",
                onBackClick = onBackClick,
                actions = {
                    TextButton(
                        onClick = onRefresh,
                        enabled = uiState.hasLocation && !uiState.isLoading
                    ) {
                        Text(
                            text = "Refresh",
                            color = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = uiState.areaName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Conditions can change quickly. Review " +
                                "official information, posted signs, and " +
                                "instructions from local authorities.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                parkCodeContent()
            }

            item {
                WeatherForecastCard(
                    uiState = uiState,
                    onRefresh = onRefresh
                )
            }

            if (uiState.hasUnavailableSources) {
                item {
                    Text(
                        text = "Some sources are unavailable. " +
                                "Results may be incomplete.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                Text(
                    text = "Safety Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                SafetyFilterRow(
                    selectedFilter = uiState.filterSelected,
                    onFilterSelected = onFilterSelected
                )
            }

            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                !uiState.hasLocation -> {
                    item {
                        Text(
                            text = "Select a trip destination or use " +
                                    "Locate Me in Explorer."
                        )
                    }
                }

                uiState.errorMessage != null -> {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.error
                            )

                            Button(onClick = onRefresh) {
                                Text("Try Again")
                            }
                        }
                    }
                }

                uiState.notifications.isEmpty() -> {
                    item {
                        Text(
                            text = "No notifications to display for this " +
                                    "filter. Check source status; an empty " +
                                    "list does not establish that the area is safe."
                        )
                    }
                }

                else -> {
                    items(
                        items = uiState.notifications,
                        key = { it.id }
                    ) { notification ->
                        SafetyNotificationCard(
                            notification = notification,
                            onSourceClick = onSourceClick
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun SafetyFilterRow(
    selectedFilter: SafetyFilter,
    onFilterSelected: (SafetyFilter) -> Unit
)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SafetyFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = {
                    onFilterSelected(filter)
                },
                label = {
                    Text(filter.displayName)
                }
            )
        }
    }
}
@Composable
private fun SafetyNotificationCard(
    notification: SafetyNotification,
    onSourceClick: (String) -> Unit
) {
    val cardColor = when (notification.level) {
        SafetyLevel.SEVERE ->
            MaterialTheme.colorScheme.errorContainer

        SafetyLevel.MODERATE ->
            MaterialTheme.colorScheme.tertiaryContainer

        SafetyLevel.MINOR ->
            MaterialTheme.colorScheme.secondaryContainer

        SafetyLevel.UNKNOWN ->
            MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = notification.category.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = notification.level.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.details,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Instructions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = notification.instruct,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Affected area: ${notification.affectedArea}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Source: ${notification.sourceID}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Retrieved: ${notification.retrievedTime}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Last verified: ${notification.lastVerification}",
                style = MaterialTheme.typography.bodySmall
            )
            notification.expires?.let { expiration ->
                Text(
                    text = "Expires: $expiration",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (notification.dataExpired) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Warning: This information has expired.",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }



            TextButton(
                onClick = {
                    onSourceClick(notification.sourceUrl)
                }
            ) {
                Text("View Source")
            }
        }
    }

}
