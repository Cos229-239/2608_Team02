package com.cos229239.team02.oto.ui.theme

import androidx.compose.ui.unit.dp

/*
 * OTO Design System — Spacing & Dimensions
 *
 * Shared layout measurements used throughout the Android app.
 * These correspond to the spacing/dimension tokens in the
 * OTO Figma Design System.
 */

object OtoSpacing {

    // -------------------------------------------------------------------------
    // Spacing
    // -------------------------------------------------------------------------

    val None = 0.dp
    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Standard = 16.dp
    val Large = 24.dp
    val XLarge = 32.dp
    val XXLarge = 48.dp

    // -------------------------------------------------------------------------
    // Screen layout
    // -------------------------------------------------------------------------

    val ScreenHorizontal = 20.dp
    val ScreenVertical = 20.dp

    // -------------------------------------------------------------------------
    // Components
    // -------------------------------------------------------------------------

    val CardPadding = 16.dp
    val CardGap = 12.dp
    val SectionGap = 24.dp
    val IconGap = 12.dp

    // -------------------------------------------------------------------------
    // Android touch targets
    // -------------------------------------------------------------------------

    // Standard Android accessibility minimum.
    val TouchTarget = 48.dp

    // Larger target for important Crisis Mode actions.
    val CrisisTouchTarget = 64.dp

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    val BottomNavigationHeight = 80.dp
}