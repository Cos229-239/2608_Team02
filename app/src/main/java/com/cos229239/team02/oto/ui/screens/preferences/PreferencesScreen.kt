package com.cos229239.team02.oto.ui.screens.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cos229239.team02.oto.data.preferences.DistanceUnit
import com.cos229239.team02.oto.ui.components.OtoTopAppBar
import com.cos229239.team02.oto.ui.preferences.AppPreferencesViewModel
import com.cos229239.team02.oto.ui.theme.OtoSpacing

//Display app-wide OTO preferences.
@Composable
fun PreferencesScreen(
    appPreferencesViewModel: AppPreferencesViewModel,
    onBackClick: () -> Unit
) {

    Scaffold(
        topBar = {
            OtoTopAppBar(
                title = "Preferences",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    horizontal = OtoSpacing.ScreenHorizontal,
                    vertical = OtoSpacing.ScreenVertical
                )
        ) {

            Text(
                text = "DISTANCE UNITS",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(
                    OtoSpacing.Small
                )
            )

            Text(
                text = "Choose how distances are displayed throughout OTO.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(
                    OtoSpacing.Standard
                )
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(
                        OtoSpacing.CardPadding
                    )
                ) {

                    //Allow the user to display distances in miles and feet.
                    DistanceUnitOption(
                        title = "Miles",
                        description = "Use miles and feet.",
                        selected =
                            appPreferencesViewModel.distanceUnit ==
                                    DistanceUnit.MILES,
                        onClick = {
                            appPreferencesViewModel.updateDistanceUnit(
                                DistanceUnit.MILES
                            )
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(
                            OtoSpacing.Standard
                        )
                    )

                    //Allow the user to display distances in kilometers and meters.
                    DistanceUnitOption(
                        title = "Kilometers",
                        description = "Use kilometers and meters.",
                        selected =
                            appPreferencesViewModel.distanceUnit ==
                                    DistanceUnit.KILOMETERS,
                        onClick = {
                            appPreferencesViewModel.updateDistanceUnit(
                                DistanceUnit.KILOMETERS
                            )
                        }
                    )
                }
            }
        }
    }
}

//Display one selectable distance-unit preference.
@Composable
private fun DistanceUnitOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Spacer(
            modifier = Modifier.width(
                OtoSpacing.Small
            )
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}