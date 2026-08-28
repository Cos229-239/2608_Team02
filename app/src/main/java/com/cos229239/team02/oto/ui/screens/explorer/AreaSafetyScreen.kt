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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cos229239.team02.oto.ui.features.AreaSafetyUIState
import com.cos229239.team02.oto.ui.features.AreaSafetyView
import com.cos229239.team02.oto.ui.features.SafetyFilter
import com.cos229239.team02.oto.ui.features.SafetyLevel
import com.cos229239.team02.oto.ui.features.SafetyNotification

@Composable
fun AreaSafetyRoute( onBackClick: () -> Unit,
                     safetyView: AreaSafetyView = viewModel()
)
{

    val uiState by safetyView.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    AreaSafetyScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRefresh = safetyView::refreshNotifications,
        onFilterSelected = safetyView::selectFilter,
        onSourceClick = { sourceUrl ->
            uriHandler.openUri(sourceUrl)
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
    onSourceClick: (String) -> Unit
)
{
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Area Safety")
                        },
                navigationIcon = { TextButton(
                    onClick = onBackClick
                ) {
                    Text("Back")
                }
                                 },
                actions = {
                    TextButton(
                        onClick = onRefresh,
                        enabled = !uiState.isLoading
                    ) {
                        Text("Refresh")
                    }
                }
            )
        }
    ){ paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = uiState.areaName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium
                ) {
                Text(
                    text = """
                        Conditions can change quickly. Review official 
                        information, posted signs, and instructions from
                        local authorities. 
                    """.trimIndent(),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (uiState.isSampleData){
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = """
                            Sample notifications do not represent current conditions. 
                        """.trimIndent(),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold

                    )
                }
            }
            if (uiState.isOffline) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = """
                            Offline: Saved information may be outdated.  
                        """.trimIndent(),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold

                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = "Safety Notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            SafetyFilterRow(
                selectedFilter = uiState.filterSelected,
                onFilterSelected = onFilterSelected
            )

            Spacer(modifier = Modifier.height(12.dp))

            val errorMessage = uiState.errorMessage

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onRefresh
                        )
                        {
                            Text("Try Again")
                        }
                    }
                }
                uiState.notifications.isEmpty() -> {
                    Text(
                        text = """
                            No Notifications Data Available. 
                            This does not mean to not be alert of potential 
                            hazards Please Be Aware
                        """.trimIndent(),
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize(),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.notifications,
                            key = { notification ->
                                notification.id
                            }
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

            if (notification.sampleData) {
                Text(
                    text = "SAMPLE DATA",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            TextButton(
                onClick = {
                    onSourceClick(notification.sourceURL)
                }
            ) {
                Text("View Source")
            }
        }
    }

}