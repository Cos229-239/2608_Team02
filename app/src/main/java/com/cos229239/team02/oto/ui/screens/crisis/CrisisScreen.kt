package com.cos229239.team02.oto.ui.screens.crisis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CrisisScreen(
    onFirstAidSurvivalClick: () -> Unit,
    onShareStatusLocationClick: () -> Unit,
    onNearbyResourcesClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Crisis Mode")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onFirstAidSurvivalClick) {
            Text(text = "First Aid & Survival")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onShareStatusLocationClick) {
            Text(text = "Share Status and Location")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onNearbyResourcesClick) {
            Text(text = "Nearby Resources")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onBackClick) {
            Text(text = "Back")
        }
    }
}
