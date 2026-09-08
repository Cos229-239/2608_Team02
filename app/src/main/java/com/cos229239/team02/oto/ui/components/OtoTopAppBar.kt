package com.cos229239.team02.oto.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//Display OTO's shared Material 3 top app bar.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtoTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null
) {

    val darkGreen = Color(0xFF063D24)

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                color = Color.White
            )
        },
        navigationIcon = {
            if (onBackClick != null) {

                TextButton(
                    onClick = onBackClick
                ) {
                    Text(
                        text = "←",
                        color = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = darkGreen
        )
    )
}