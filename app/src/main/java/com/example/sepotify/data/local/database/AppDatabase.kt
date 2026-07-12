package com.example.sepotify.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sepotify.data.local.database.converters.InstantConverter
import com.example.sepotify.data.local.database.dao.DownloadedSongDao
import com.example.sepotify.data.local.database.dao.FollowDao
import com.example.sepotify.data.local.database.dao.MessageDao
import com.example.sepotify.data.local.database.dao.SearchHistoryDao
import com.example.sepotify.data.local.database.entity.*

@Database(entities = [SearchHistory::class, MessageEntity::class, FollowEntity::class, DownloadedSongEntity::class], version = 4, exportSchema = false)
@TypeConverters(InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun messageDao(): MessageDao
    abstract fun followDao(): FollowDao
    abstract fun downloadedSongDao(): DownloadedSongDao
}