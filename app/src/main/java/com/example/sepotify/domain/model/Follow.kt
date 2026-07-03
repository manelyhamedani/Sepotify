package com.example.sepotify.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Follow(
    val id: Long = 0,
    @SerialName("follower_id")
    val followerId: String = "",
    @SerialName("followed_id")
    val followedId: String = "",
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)