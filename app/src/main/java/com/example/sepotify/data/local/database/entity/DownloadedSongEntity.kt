package com.example.sepotify.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_songs")
data class DownloadedSongEntity(
    @PrimaryKey val songId: String,
    val songData: String,       // JSON representation of Song
    val filePath: String,
    val coverPath: String? = null,
    val downloadedAt: Long
)