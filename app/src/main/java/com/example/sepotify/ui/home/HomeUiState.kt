package com.example.sepotify.ui.home

import com.example.sepotify.domain.model.Playlist
import com.example.sepotify.domain.model.Song

data class HomeUiState(
    val trendingSongs: List<Song> = emptyList(),
    val newReleases: List<Song> = emptyList(),
    val globalPlaylists: List<Playlist> = emptyList(),
    val localPlaylists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)