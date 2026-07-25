package com.example.sepotify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sepotify.data.local.preferences.SettingsDataStoreManager
import com.example.sepotify.ui.navigation.NavGraph
import com.example.sepotify.ui.theme.AppTheme
import com.example.sepotify.ui.theme.FontSizeScale
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()
        }
    }
}

// In your top‑level composable (e.g., MainActivity or AppNavHost)
@Composable
fun MyApp() {
    // Read settings from DataStore
    val settingsManager: SettingsDataStoreManager = koinInject()
    val themeMode by settingsManager.themeModeFlow.collectAsState(initial = "system")
    val fontScale by settingsManager.fontScaleFlow.collectAsState(initial = "medium")

    // Convert to Booleans / FontSizeScale
    val darkMode = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val fontSize = when (fontScale) {
        "small" -> FontSizeScale.SMALL
        "large" -> FontSizeScale.LARGE
        else -> FontSizeScale.MEDIUM
    }

    AppTheme(
        darkMode = darkMode,
        fontSizeScale = fontSize
    ) {
        // Your app navigation
        NavGraph()
    }
}