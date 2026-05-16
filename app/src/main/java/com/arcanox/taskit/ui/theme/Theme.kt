package com.arcanox.taskit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = StitchPrimary,
    onPrimary = StitchOnPrimary,
    primaryContainer = StitchPrimaryContainer,
    onPrimaryContainer = StitchOnPrimaryContainer,
    secondary = StitchSecondary,
    onSecondary = StitchOnSecondary,
    secondaryContainer = StitchSecondaryContainer,
    onSecondaryContainer = StitchOnSecondaryContainer,
    tertiary = StitchTertiary,
    onTertiary = StitchOnTertiary,
    tertiaryContainer = StitchTertiaryContainer,
    onTertiaryContainer = StitchOnTertiaryContainer,
    background = StitchSurface,
    onBackground = StitchOnSurface,
    surface = StitchSurfaceContainer,
    onSurface = StitchOnSurface,
    surfaceVariant = StitchSurfaceVariant,
    onSurfaceVariant = StitchOnSurfaceVariant,
    error = StitchError,
    onError = StitchOnError
)

private val LightColorScheme = lightColorScheme(
    primary = StitchPrimaryLight,
    onPrimary = StitchOnPrimaryLight,
    secondary = StitchSecondaryLight,
    onSecondary = StitchOnSecondaryLight,
    tertiary = StitchTertiaryLight,
    onTertiary = StitchOnTertiaryLight,
    background = StitchSurfaceLight,
    onBackground = StitchOnSurfaceLight,
    surface = StitchSurfaceContainerLight,
    onSurface = StitchOnSurfaceLight,
    surfaceVariant = StitchSurfaceContainerLight,
    onSurfaceVariant = StitchOnSurfaceVariantLight,
)

@Composable
fun TasKitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
