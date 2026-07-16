package com.example.sepotify.ui.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sepotify.data.repository.MusicRepository
import com.example.sepotify.domain.model.Song
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadsViewModel(
    private val musicRepo: MusicRepository,
    userId: String
) : ViewModel() {
    val downloadedSongs: StateFlow<List<Song>> = musicRepo.getDownloadedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun deleteDownload(songId: String) {
        viewModelScope.launch {
            musicRepo.deleteDownloadedSong(songId)
        }
    }
}