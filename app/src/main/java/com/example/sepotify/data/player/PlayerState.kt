package com.example.sepotify.data.player

import com.example.sepotify.domain.model.Song

data class PlayerState(

    val currentSong: Song? = null,

    val queue: List<Song> = emptyList(),

    val currentIndex: Int = 0,

    val isPlaying: Boolean = false,

    val currentPosition: Long = 0L,

    val duration: Long = 0L,

    val bufferedPosition: Long = 0L,

    val playbackSpeed: Float = 1f,

    val shuffle: Boolean = false,

    val repeatMode: Int = 0,

    val sleepTimerRemaining: Long = 0L
)