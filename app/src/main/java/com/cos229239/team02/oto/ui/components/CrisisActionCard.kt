package com.cos229239.team02.oto.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.cos229239.team02.oto.ui.theme.OtoCrisisRed
import com.cos229239.team02.oto.ui.theme.OtoSpacing

/*
 * OTO Design System — Crisis Action Card
 *
 * Reusable card for the six primary Crisis Mode actions.
 *
 * The card owns the shared layout and styling.
 * Each screen supplies its own title, description, and click action.
 */

@Composable
fun CrisisActionCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OtoSpacing.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OtoSpacing.IconGap)
        ) {

            /*
             * Crisis accent marker.
             *
             * This is intentionally simple for the first implementation.
             * We can replace it with the approved Crisis icon asset later.
             */
            Surface(
                modifier = Modifier
                    .size(OtoSpacing.TouchTarget)
                    .clip(MaterialTheme.shapes.medium),
                color = OtoCrisisRed,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "!",
                    modifier = Modifier.padding(top = 7.dp),
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(OtoSpacing.XSmall)
                )

                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "›",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
