package com.cos229239.team02.oto.ui.screens.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlanTripScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {

    // These variables hold what the user types into the form.
    // Later, API or database logic can use this information.
    var startingPoint by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var estimatedTime by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var estimatedReturn by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Keeps track of which trip type the user selects.
    var isRoundTrip by remember { mutableStateOf(true) }

    val darkGreen = Color(0xFF0B5D1E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // Top green header.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(darkGreen)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Back button.
            TextButton(
                onClick = onBackClick
            ) {
                Text(
                    text = "←",
                    color = Color.White,
                    fontSize = 24.sp
                )
            }

            Text(
                text = "PLAN YOUR TRIP",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top
        ) {

            // Round Trip / One Way buttons.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = {
                        isRoundTrip = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRoundTrip) {
                            darkGreen
                        } else {
                            Color.LightGray
                        }
                    )
                ) {
                    Text(text = "Round Trip")
                }

                Button(
                    onClick = {
                        isRoundTrip = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isRoundTrip) {
                            darkGreen
                        } else {
                            Color.LightGray
                        }
                    )
                ) {
                    Text(text = "One Way")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "STARTING POINT")

            OutlinedTextField(
                value = startingPoint,
                onValueChange = {
                    startingPoint = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = "Enter starting point")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "DESTINATION")

            OutlinedTextField(
                value = destination,
                onValueChange = {
                    destination = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = "Enter destination")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Distance and estimated 50time share one row.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "DISTANCE")

                    OutlinedTextField(
                        value = distance,
                        onValueChange = {
                            distance = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Distance")
                        }
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "EST. TIME")

                    OutlinedTextField(
                        value = estimatedTime,
                        onValueChange = {
                            estimatedTime = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Time")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "DATE")

            OutlinedTextField(
                value = date,
                onValueChange = {
                    date = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = "Select date")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "EST. RETURN")

            OutlinedTextField(
                value = estimatedReturn,
                onValueChange = {
                    estimatedReturn = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = "Estimated return time")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "NOTES")

            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = {
                    Text(text = "Add notes about your trip")
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkGreen
                )
            ) {
                Text(text = "SAVE TRIP")
            }
        }
    }
}