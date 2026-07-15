package com.example.sepotify.data.player

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sepotify.data.repository.MusicRepository
import com.example.sepotify.domain.model.Song
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import androidx.core.net.toUri

@OptIn(UnstableApi::class)
class PlayerManager(
    private val musicRepo: MusicRepository,
    application: Application
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controller: MediaController? = null
    private val controllerDeferred = CompletableDeferred<MediaController>()
    private var currentQueue: List<Song> = emptyList()
    private var sleepTimerJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var lastAddedSongId: String? = null
    private val _userId = MutableStateFlow<String?>(null)

    fun setUserId(userId: String) {
        _userId.value = userId
    }

    init {
        val sessionToken = SessionToken(
            application,
            ComponentName(application, MusicService::class.java)
        )

        val controllerFuture = MediaController.Builder(
            application,
            sessionToken
        ).buildAsync()

        controllerFuture.addListener({
            val mediaController = controllerFuture.get()
            controller = mediaController
            mediaController.addListener(playerListener)
            startProgressUpdates()
            updateState()
            controllerDeferred.complete(mediaController)
            Log.d("PLAYER", "Controller connected")
        }, MoreExecutors.directExecutor())
    }

    private suspend fun awaitController(): MediaController {
        return controller ?: controllerDeferred.await()
    }

    private fun Song.toMediaItem(uri: Uri, localCoverPath: String? = null): MediaItem  {
        val artworkUri = localCoverPath?.let { Uri.fromFile(File(it)) }
            ?: coverUrl?.toUri()

        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(
                        artists.joinToString(", ") { it.name }
                    )
                    .setArtworkUri(coverUrl?.toUri())
                    .build()
            )
            .build()
    }


    private val playerListener = object : Player.Listener {
        override fun onEvents(
            player: Player,
            events: Player.Events
        ) {
            updateState()
            Log.d(
                "PLAYER",
                "state=${player.playbackState}, playing=${player.isPlaying}"
            )
        }
        override fun onPlayerError(error: PlaybackException) {
            Log.e("PLAYER", "Playback error", error)
        }

        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int
        ) {
            super.onMediaItemTransition(mediaItem, reason)
            val songId = mediaItem?.mediaId
            val userId = _userId.value
            if (songId != null && userId != null && songId != lastAddedSongId) {
                lastAddedSongId = songId
                CoroutineScope(Dispatchers.IO).launch {
                    musicRepo.addToRecentlyPlayed(userId, songId)
                }
            }
            updateState()
        }

        override fun onIsPlayingChanged(
            isPlaying: Boolean
        ) {
            updateState()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                currentSong()?.id?.let { songId ->
                    CoroutineScope(Dispatchers.IO).launch {
                        musicRepo.incrementPlayCount(songId)
                    }
                }
            }
        }
    }

    private fun startProgressUpdates() {
        scope.launch {
            while(isActive) {
                updateState()
                delay(500)
            }
        }
    }

    private fun updateState() {
        scope.launch {
            val player = awaitController()

            val currentSong = currentQueue.getOrNull(player.currentMediaItemIndex)

            _playerState.value = _playerState.value.copy(
                currentSong = currentSong,
                queue = currentQueue,
                currentIndex = player.currentMediaItemIndex,
                isPlaying = player.isPlaying,
                currentPosition = player.currentPosition,
                duration = player.duration.coerceAtLeast(0),
                bufferedPosition = player.bufferedPosition,
                playbackSpeed = player.playbackParameters.speed,
                shuffle = player.shuffleModeEnabled,
                repeatMode = player.repeatMode,
            )
        }
    }

    fun setSleepTimer(delayMillis: Long) {
        cancelSleepTimer()
        sleepTimerJob = scope.launch {
            var remaining = delayMillis
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000
                _playerState.value = _playerState.value.copy(
                    sleepTimerRemaining = remaining
                )
            }
            stop()
            _playerState.value = _playerState.value.copy(
                sleepTimerRemaining = 0L
            )
        }
        _playerState.value = _playerState.value.copy(
            sleepTimerRemaining = delayMillis
        )
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _playerState.value = _playerState.value.copy(
            sleepTimerRemaining = 0L
        )
    }

    fun play() {
        scope.launch {
            awaitController().play()
        }
    }

    fun pause() {
        scope.launch {
            awaitController().pause()
        }
    }

    fun stop() {
        scope.launch {
            val player = awaitController()
            player.stop()
            player.clearMediaItems()
            cancelSleepTimer()
            currentQueue = emptyList()
            updateState()
        }
    }

    fun playPause() {
        scope.launch {
            awaitController().let {
                if (it.isPlaying) it.pause()
                else it.play()
            }
        }
    }

    fun next() {
        scope.launch {
            val player = awaitController()
            if (player.currentMediaItemIndex == currentQueue.size) {
                player.seekTo(0)
            } else {
                player.seekToNextMediaItem()
            }
        }
    }

    fun previous() {
        scope.launch {
            val player = awaitController()
            if (player.currentMediaItemIndex == 0) {
                player.seekTo(currentQueue.size.toLong())
            } else if (player.currentPosition > 3000) {
                player.seekTo(0)
            } else {
                player.seekToPreviousMediaItem()
            }
        }
    }

    fun seekTo(position: Long) {
        scope.launch {
            awaitController().seekTo(position)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        scope.launch {
            awaitController().setPlaybackSpeed(speed)
        }
    }

    fun toggleShuffle() {
        scope.launch {
            awaitController().let {
                it.shuffleModeEnabled = !it.shuffleModeEnabled
            }
        }
    }

    fun toggleRepeat() {
        scope.launch {
            awaitController().let {
                it.repeatMode = when (it.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
            }
        }
    }

    fun playQueue(
        songs: List<Song>,
        startIndex: Int = 0
    ) {
        scope.launch {
            Log.d("PLAYER", "playQueue: ${songs.size} songs, index=$startIndex")
            lastAddedSongId = null
            val player = awaitController()

            currentQueue = songs

            val items = songs.map { song ->
                val (uri, coverPath) = getSongInfo(song)
                song.toMediaItem(uri, coverPath) // pass the uri
            }

            songs.forEach {
                Log.d("PLAYER", "URL = ${it.audioUrl}")
            }

            player.setMediaItems(
                items,
                startIndex,
                0L
            )
            Log.d("PLAYER", "MediaItems=${player.mediaItemCount}")
            player.prepare()

            player.play()

            Log.d(
                "PLAYER",
                "playWhenReady=${player.playWhenReady}"
            )
        }
    }

    fun playSong(song: Song) {
        playQueue(listOf(song), 0)
    }

    fun addToQueue(song: Song) {
        scope.launch {
            val player = awaitController()
            currentQueue += song
            val (uri, coverPath) = getSongInfo(song)
            player.addMediaItem(song.toMediaItem(uri, coverPath))
        }
    }

    fun currentSong(): Song? = playerState.value.currentSong

    fun currentQueue(): List<Song> = playerState.value.queue

    suspend fun release() {
        awaitController().removeListener(playerListener)
        awaitController().release()
        scope.cancel()
    }

    private suspend fun getSongInfo(song: Song): Pair<Uri, String?> {
        val localPath = musicRepo.getLocalFilePath(song.id)
        if (!localPath.isNullOrEmpty()) {
            Log.d("PLAYER", "Playing from local: $localPath")
            val uri = Uri.fromFile(File(localPath))
            val coverPath = musicRepo.getLocalCoverPath(song.id)
            return uri to coverPath
        } else {
            Log.d("PLAYER", "Playing from network: ${song.audioUrl}")
            val uri = song.audioUrl.toUri()
            return uri to song.coverUrl
        }
    }

}