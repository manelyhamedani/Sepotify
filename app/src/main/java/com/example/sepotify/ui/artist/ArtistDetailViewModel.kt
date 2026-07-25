// ArtistDetailViewModel.kt
package com.example.sepotify.ui.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.example.sepotify.data.repository.MusicRepository
import com.example.sepotify.domain.model.Artist
import com.example.sepotify.domain.model.Song
import com.example.sepotify.utils.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ArtistDetailUiState {
    object Loading : ArtistDetailUiState()
    data class Success(
        val artist: Artist,
        val songs: Flow<PagingData<Song>>
    ) : ArtistDetailUiState()
    data class Error(val message: String) : ArtistDetailUiState()
}

class ArtistDetailViewModel(
    private val musicRepo: MusicRepository,
    artistId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArtistDetailUiState>(ArtistDetailUiState.Loading)
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    init {
        loadArtist(artistId)
    }

    private fun loadArtist(artistId: String) {
        viewModelScope.launch {
            _uiState.value = ArtistDetailUiState.Loading
            when (val result = musicRepo.getArtist(artistId)) {
                is Result.Success -> {
                    val songsFlow = musicRepo.getSongsByArtists(artistId)
                    _uiState.value = ArtistDetailUiState.Success(result.data, songsFlow)
                }
                is Result.Error -> _uiState.value = ArtistDetailUiState.Error(result.message)
            }
        }
    }
}