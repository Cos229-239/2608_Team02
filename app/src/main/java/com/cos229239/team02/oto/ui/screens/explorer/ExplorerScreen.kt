package com.cos229239.team02.oto.ui.screens.explorer

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
fun ExplorerScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Explorer Mode")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { }) {
            Text(text = "Plan Trip")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { }) {
            Text(text = "Create New Route")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { }) {
            Text(text = "Offline Maps")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { }) {
            Text(text = "Area Safety")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { }) {
            Text(text = "Report Hazard")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onBackClick) {
            Text(text = "Back")
        }
    }
}

