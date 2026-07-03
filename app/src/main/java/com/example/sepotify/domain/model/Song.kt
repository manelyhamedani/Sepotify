package com.example.sepotify.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val id: String = "",
    val title: String = "",
    @SerialName("cover_url")
    val coverUrl: String? = null,
    @SerialName("audio_url")
    val audioUrl: String,
    @SerialName("duration_seconds")
    val durationSeconds: Int,
    @SerialName("play_count")
    val playCount: Long = 0,
    val artists: List<Artist> = emptyList()
)