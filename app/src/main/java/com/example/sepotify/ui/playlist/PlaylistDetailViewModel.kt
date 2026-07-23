// PlaylistDetailViewModel.kt
package com.example.sepotify.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sepotify.data.repository.MusicRepository
import com.example.sepotify.domain.model.Playlist
import com.example.sepotify.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlaylistDetailUiState {
    object Loading : PlaylistDetailUiState()
    data class Success(val playlist: Playlist) : PlaylistDetailUiState()
    data class Error(val message: String) : PlaylistDetailUiState()
}

class PlaylistDetailViewModel(
    private val musicRepo: MusicRepository,
    playlistId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlaylistDetailUiState>(PlaylistDetailUiState.Loading)
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    init {
        loadPlaylist(playlistId)
    }

    private fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            _uiState.value = PlaylistDetailUiState.Loading
            when (val result = musicRepo.getPlaylist(playlistId)) {
                is Result.Success -> _uiState.value = PlaylistDetailUiState.Success(result.data)
                is Result.Error -> _uiState.value = PlaylistDetailUiState.Error(result.message)
            }
        }
    }
}