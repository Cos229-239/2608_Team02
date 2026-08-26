package com.cos229239.team02.oto.ui.screens.crisis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FirstAidSurvivalScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Text("\u2190", style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    text = "First Aid & Survival",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FirstAidSection(
                title = "Bleeding Control",
                content = "1. Apply direct pressure with clean cloth\n2. Elevate the injured area\n3. Apply pressure bandage\n4. If severe, apply tourniquet 2-3 inches above wound\n5. Do not remove blood-soaked cloths"
            )

            FirstAidSection(
                title = "Fractures & Sprains",
                content = "1. Immobilize the injured area\n2. Splint using branches, trekking poles, or rolled clothing\n3. Apply ice wrapped in cloth (20 min on, 20 min off)\n4. Do not attempt to realign bones"
            )

            FirstAidSection(
                title = "Hypothermia",
                content = "1. Move to shelter immediately\n2. Remove wet clothing, replace with dry layers\n3. Warm the core first (neck, head, groin)\n4. Drink warm fluids (no alcohol)\n5. Share body heat if with a partner"
            )

            FirstAidSection(
                title = "Heat Exhaustion / Heat Stroke",
                content = "1. Move to shade immediately\n2. Loosen clothing\n3. Apply cool water to skin\n4. Drink water slowly\n5. If confusion or loss of consciousness, call 911"
            )

            FirstAidSection(
                title = "Navigation Without Signal",
                content = "1. Stay put if possible\n2. Use offline map to find trail\n3. Follow water downstream to find civilization\n4. Use whistle (3 blasts = SOS)\n5. Signal mirror for aircraft"
            )

            FirstAidSection(
                title = "Wildlife Encounters",
                content = "Bears: Make noise, back away slowly, do not run\nMountain Lions: Make yourself large, yell, fight back\nSnakes: Back away, do not attempt to catch\nAll: Give animals space and escape route"
            )

            FirstAidSection(
                title = "Water Purification",
                content = "1. Boil water for at least 1 minute\n2. Use purification tablets per instructions\n3. Use portable filter if available\n4. Solar disinfection in clear bottle (6+ hours)\n5. Running water is safer than still water"
            )
        }
    }
}

@Composable
private fun FirstAidSection(
    title: String,
    content: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
