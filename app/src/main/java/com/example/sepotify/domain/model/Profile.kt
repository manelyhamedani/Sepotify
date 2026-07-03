package com.example.sepotify.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile (
    val id: String = "",
    val username: String = "",
    @SerialName("full_name")
    val fullName: String = "",
    @SerialName("avatar_url")
    val avatarUrl: String = "",
    @SerialName("is_premium")
    val isPremium: Boolean = false
)