package com.example.sepotify.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistSongInsert(
    @SerialName("playlist_id")
    val playlistId: String,
    @SerialName("song_id")
    val songId: String,
    val position: Int
)