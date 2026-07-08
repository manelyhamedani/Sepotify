package com.example.sepotify.data.local.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsPreferences {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val FONT_SCALE = stringPreferencesKey("font_scale")

    // Default values
    const val DEFAULT_THEME_MODE = "system" // "light", "dark", "system"
    const val DEFAULT_FONT_SCALE = "medium" // "small", "medium", "large"
}