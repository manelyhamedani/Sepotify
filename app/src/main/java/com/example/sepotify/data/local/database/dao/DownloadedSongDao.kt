package com.example.sepotify.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sepotify.data.local.database.entity.DownloadedSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadedSongEntity)

    @Query("DELETE FROM downloaded_songs WHERE songId = :songId")
    suspend fun delete(songId: String)

    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun getAll(): Flow<List<DownloadedSongEntity>>

    @Query("SELECT filePath FROM downloaded_songs WHERE songId = :songId")
    suspend fun getFilePath(songId: String): String?

    @Query("SELECT coverPath FROM downloaded_songs WHERE songId = :songId")
    suspend fun getCoverPath(songId: String): String?

    @Query("SELECT * FROM downloaded_songs WHERE songId = :songId LIMIT 1")
    suspend fun getSong(songId: String): DownloadedSongEntity?
}