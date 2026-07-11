package com.example.sepotify.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "follows")
data class FollowEntity(
    @PrimaryKey val id: Long = 0,
    @SerialName("follower_id")
    val followerId: String,
    @SerialName("followed_id")
    val followedId: String,
    @SerialName("created_at")
    val createdAt: Instant
)