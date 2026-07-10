// file: ui/theme/Theme.kt
package com.example.sepotify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

// State to hold theme preferences
data class AppThemeState(
    val darkMode: Boolean = false,
    val fontSizeScale: FontSizeScale = FontSizeScale.MEDIUM
)

enum class FontSizeScale {
    SMALL,
    MEDIUM,
    LARGE
}

// CompositionLocal for theme state (so we can read/write from anywhere)
val LocalAppThemeState = compositionLocalOf { AppThemeState() }

// Main theme composable
@Composable
fun AppTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    fontSizeScale: FontSizeScale = FontSizeScale.MEDIUM,
    content: @Composable () -> Unit
) {
    // Provide the state to descendants
    val themeState = remember { AppThemeState(darkMode, fontSizeScale) }

    // Select colors based on dark mode
    val colors = if (darkMode) {
        darkColorScheme(
            primary = DarkPrimary,
            onPrimary = DarkOnPrimary,
            primaryContainer = DarkPrimaryVariant,
            onPrimaryContainer = DarkOnPrimary,
            secondary = DarkSecondary,
            onSecondary = DarkOnSecondary,
            background = DarkBackground,
            onBackground = DarkOnBackground,
            surface = DarkSurface,
            onSurface = DarkOnSurface,
            error = DarkError,
            onError = DarkOnPrimary
        )
    } else {
        lightColorScheme(
            primary = LightPrimary,
            onPrimary = LightOnPrimary,
            primaryContainer = LightPrimaryVariant,
            onPrimaryContainer = LightOnPrimary,
            secondary = LightSecondary,
            onSecondary = LightOnSecondary,
            background = LightBackground,
            onBackground = LightOnBackground,
            surface = LightSurface,
            onSurface = LightOnSurface,
            error = LightError,
            onError = LightOnPrimary
        )
    }

    // Select typography based on font scale
    val typography = when (fontSizeScale) {
        FontSizeScale.SMALL -> SmallTypography
        FontSizeScale.MEDIUM -> MediumTypography
        FontSizeScale.LARGE -> LargeTypography
    }

    // Provide the state and apply MaterialTheme
    CompositionLocalProvider(
        LocalAppThemeState provides themeState
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            shapes = AppShapes,
            content = content
        )
    }
}

// Extension to access theme state easily
@Composable
fun useAppThemeState(): AppThemeState {
    return LocalAppThemeState.current
}

// Utility to toggle dark mode (to be used in ViewModels or Composable)
@Composable
fun rememberToggleDarkMode(): () -> Unit {
    val state = useAppThemeState()
    // We'll need a mutable state holder; we'll update via a callback that changes the parent state
    // This will be integrated with DataStore later; for now we just return a no-op placeholder
    return {
        // This will be implemented when we connect to DataStore
    }
}