package com.example.sepotify.ui.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sepotify.data.player.PlayerManager
import com.example.sepotify.data.repository.MusicRepository
import com.example.sepotify.domain.model.DownloadState
import com.example.sepotify.domain.model.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.sepotify.utils.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PlayerViewModel(
    private val playerManager: PlayerManager,
    private val musicRepo: MusicRepository,
    private val userId: String
) : ViewModel() {

    val playerState = playerManager.playerState
    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val userPlaylists: StateFlow<List<Playlist>> = _userPlaylists.asStateFlow()

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.NotStarted)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private var downloadJob: Job? = null

    init {
        viewModelScope.launch {
            playerState
                .map { it.currentSong?.id }
                .distinctUntilChanged()
                .collect { songId ->
                    downloadJob?.cancel()
                    downloadJob = null

                    if (songId != null) {
                        val isDownloaded = musicRepo.isSongDownloaded(songId)
                        if (isDownloaded) {
                            _downloadState.value = DownloadState.Completed
                        }
                        else {
                            _downloadState.value = DownloadState.NotStarted
                            downloadJob = viewModelScope.launch {
                                musicRepo.getDownloadStatus(songId).collect { state ->
                                    if (state !is DownloadState.Completed) {
                                        _downloadState.value = state
                                    }
                                }
                            }
                            if (_downloadState.value !is DownloadState.Downloading) {
                                _downloadState.value = DownloadState.NotStarted
                            }
                        }
                        when (val result = musicRepo.isSongLiked(songId, userId)) {
                            is Result.Success -> _isLiked.value = result.data
                            is Result.Error -> {
                                _isLiked.value = false
                                Result.Error(result.message)
                            }
                        }
                    } else {
                        _isLiked.value = false
                        _downloadState.value = DownloadState.NotStarted
                    }
                }
        }
    }


    fun loadUserPlaylists() {
        viewModelScope.launch {
            val result = musicRepo.getUserPlaylists(userId)
            if (result is Result.Success) {
                _userPlaylists.value = result.data
            }
        }
    }

    fun addCurrentSongToPlaylist(playlistId: String) {
        val songId = playerState.value.currentSong?.id ?: return
        viewModelScope.launch {
            musicRepo.addSongToPlaylist(playlistId, songId)
        }
    }

    fun playPause() = playerManager.playPause()

    fun stopPlayback() = playerManager.stop()

    fun next() = playerManager.next()

    fun previous() = playerManager.previous()

    fun seekTo(position: Long) = playerManager.seekTo(position)

    fun setPlaybackSpeed(speed: Float) = playerManager.setPlaybackSpeed(speed)

    fun setSleepTimer(minutes: Int) {
        playerManager.setSleepTimer(minutes * 60 * 1000L)
    }

    fun cancelSleepTimer() {
        playerManager.cancelSleepTimer()
    }

    fun toggleShuffle() = playerManager.toggleShuffle()

    fun toggleRepeat() = playerManager.toggleRepeat()

    fun likeSong(songId: String) {
        viewModelScope.launch {
            val result = musicRepo.addToFavorites(userId, songId)
            if (result is Result.Success) {
                _isLiked.value = true
            }
        }
    }

    fun unlikeSong(songId: String) {
        viewModelScope.launch {
            val result = musicRepo.removeFromFavorites(userId, songId)
            if (result is Result.Success) {
                _isLiked.value = false
            }
        }
    }

    suspend fun downloadCurrentSong(): Result<String> {
        val songId = playerState.value.currentSong?.id ?: return Result.Error("No song playing")
        Log.d("PlayerViewModel", "going to download current song = $songId")
        if (musicRepo.isSongDownloaded(songId)) {
            _downloadState.value = DownloadState.Completed
            return Result.Success("Already downloaded")
        }
        _downloadState.value = DownloadState.Pending
        val result = musicRepo.downloadSong(songId, userId)
        if (result is Result.Success) {
            observeDownloadStatus(songId)
            return Result.Success("Download started")
        }
        else if (result is Result.Error) {
            if (result.message == "Premium required to download") {
                _downloadState.value = DownloadState.NotStarted
                return Result.Error(result.message)
            } else {
                // Other errors (network, etc.) show the error icon
                _downloadState.value = DownloadState.Failed
                return Result.Error("Download failed: ${result.message}")
            }
        }
        return Result.Error("Download failed")
    }

    fun observeDownloadStatus(songId: String) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            musicRepo.getDownloadStatus(songId).collect { state ->
                _downloadState.value = state
            }
        }
    }
}