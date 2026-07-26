package com.example.sepotify.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sepotify.data.local.preferences.SettingsDataStoreManager
import com.example.sepotify.ui.theme.FontSizeScale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: String = "system", // "light", "dark", "system"
    val fontScale: String = "medium"  // "small", "medium", "large"
)

class SettingsViewModel(
    private val settingsManager: SettingsDataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.themeModeFlow
                .combine(settingsManager.fontScaleFlow) { theme, font ->
                    SettingsUiState(themeMode = theme, fontScale = font)
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsManager.setThemeMode(mode)
        }
    }

    fun setFontScale(scale: String) {
        viewModelScope.launch {
            settingsManager.setFontScale(scale)
        }
    }
}