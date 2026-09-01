package com.cos229239.team02.oto.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * OTO Design System — Shapes
 *
 * Shared corner-radius rules for OTO components.
 */

val OtoShapes = Shapes(

    // Small controls, chips, compact information
    extraSmall = RoundedCornerShape(6.dp),

    // Buttons and smaller cards
    small = RoundedCornerShape(10.dp),

    // Standard cards and tiles
    medium = RoundedCornerShape(16.dp),

    // Large mode cards and major containers
    large = RoundedCornerShape(22.dp),

    // Large overlays / prominent panels
    extraLarge = RoundedCornerShape(28.dp)
)
