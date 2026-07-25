package com.example.sepotify.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    val id: String = "",
    val name: String = "",
    val bio: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)