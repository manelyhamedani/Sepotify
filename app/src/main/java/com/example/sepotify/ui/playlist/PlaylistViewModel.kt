package com.example.sepotify.ui.playlist

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.sepotify.data.repository.MusicRepository
import com.example.sepotify.domain.model.Playlist
import com.example.sepotify.domain.model.PlaylistType
import com.example.sepotify.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlaylistsUiState {
    object Loading : PlaylistsUiState()
    data class Success(val pagingFlow: Flow<PagingData<Playlist>>) : PlaylistsUiState()
    data class Error(val message: String) : PlaylistsUiState()
}

class PlaylistsViewModel(
    private val musicRepo: MusicRepository,
    private val userId: String,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<PlaylistsUiState>(PlaylistsUiState.Loading)
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()

    init {
        loadPlaylists(userId)
    }

    fun loadPlaylists(userId: String) {
        _uiState.value = PlaylistsUiState.Success(musicRepo.getAllPlaylists(userId))
    }

    fun createPlaylist(name: String, description: String?, isPublic: Boolean, coverUri: Uri?) {
        viewModelScope.launch {
            val playlist = Playlist(
                id = "",
                name = name,
                description = description,
                coverUrl = "",
                type = PlaylistType.USER,
                isPublic = isPublic,
                userId = userId,
                songs = emptyList()
            )
            when (val result = musicRepo.createPlaylist(playlist)) {
                is Result.Success -> {
                    val created = result.data
                    // Upload cover if selected
                    coverUri?.let {
                        when (val uploadResult = musicRepo.uploadPlaylistCover(created.id, it, getApplication())) {
                            is Result.Success -> {
                                musicRepo.updatePlaylistCover(created.id, uploadResult.data)
                            }
                            is Result.Error -> {
                                // Optionally show error (via snackbar)
                            }
                        }
                    }
                    loadPlaylists(userId)
                }
                is Result.Error -> {
                    // Show error (via state)
                    _uiState.value = PlaylistsUiState.Error(result.message)
                }
            }
        }
    }
}