package com.cos229239.team02.oto.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val OtoDarkColorScheme = darkColorScheme(
    primary = OtoExplorerGreen,
    onPrimary = OtoForest950,
    primaryContainer = OtoForest700,
    onPrimaryContainer = OtoTextOnDark,

    secondary = OtoExplorerGreenDark,
    onSecondary = OtoTextOnDark,

    tertiary = OtoWarningAmber,
    onTertiary = OtoForest950,

    error = OtoCrisisRed,
    onError = OtoTextOnDark,
    errorContainer = OtoCrisisRedDark,
    onErrorContainer = OtoTextOnDark,

    background = OtoForest950,
    onBackground = OtoTextOnDark,

    surface = OtoForest900,
    onSurface = OtoTextOnDark,

    surfaceVariant = OtoForest800,
    onSurfaceVariant = OtoTextOnDark,

    outline = OtoOutlineStrong
)

private val OtoLightColorScheme = lightColorScheme(
    primary = OtoForest700,
    onPrimary = OtoTextOnDark,
    primaryContainer = OtoExplorerGreenContainer,
    onPrimaryContainer = OtoForest950,

    secondary = OtoExplorerGreenDark,
    onSecondary = OtoTextOnDark,

    tertiary = OtoWarningAmber,
    onTertiary = OtoForest950,

    error = OtoCrisisRed,
    onError = OtoTextOnDark,
    errorContainer = OtoCrisisRedContainer,
    onErrorContainer = OtoCrisisRedDark,

    background = OtoBackground,
    onBackground = OtoTextPrimary,

    surface = OtoSurface,
    onSurface = OtoTextPrimary,

    surfaceVariant = OtoSurfaceSoft,
    onSurfaceVariant = OtoTextSecondary,

    outline = OtoOutline
)

@Composable
fun OTOTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> OtoDarkColorScheme
        else -> OtoLightColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as Activity).window

        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.background.toArgb()

        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = OtoShapes,
        content = content
    )
}

