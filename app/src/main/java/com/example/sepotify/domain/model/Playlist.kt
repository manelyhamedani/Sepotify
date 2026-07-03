package com.example.sepotify.domain.model

import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

// domain/model/PlaylistType.kt
@Serializable
enum class PlaylistType {
    @SerialName("GLOBAL")
    GLOBAL,
    @SerialName("LOCAL")
    LOCAL,
    @SerialName("USER")
    USER
}

@Serializable
data class Playlist(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    @SerialName("cover_url")
    val coverUrl: String? = null,
    val type: PlaylistType = PlaylistType.GLOBAL,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("country_code")
    val countryCode: String? = null,
    @SerialName("is_public")
    val isPublic: Boolean = true,
    @SerialName("created_at")
    val createdAt: Instant = Clock.System.now(),
    val songs: List<Song> = emptyList()
)