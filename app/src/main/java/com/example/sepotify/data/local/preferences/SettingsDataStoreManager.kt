package com.example.sepotify.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings"
)

class SettingsDataStoreManager(context: Context) {
    private val dataStore = context.dataStore

    // Flow for theme mode
    val themeModeFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[SettingsPreferences.THEME_MODE] ?: SettingsPreferences.DEFAULT_THEME_MODE
        }

    // Flow for font scale
    val fontScaleFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[SettingsPreferences.FONT_SCALE] ?: SettingsPreferences.DEFAULT_FONT_SCALE
        }

    // Suspend functions to update preferences
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferences.THEME_MODE] = mode
        }
    }

    suspend fun setFontScale(scale: String) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferences.FONT_SCALE] = scale
        }
    }
}