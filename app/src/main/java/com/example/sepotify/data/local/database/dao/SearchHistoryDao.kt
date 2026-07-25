package com.example.sepotify.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sepotify.data.local.database.entity.SearchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SearchHistory)

    @Query("SELECT * FROM search_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAll(userId: String): Flow<List<SearchHistory>>

    @Delete
    suspend fun delete(history: SearchHistory)

    @Query("DELETE FROM search_history WHERE userId = :userId")
    suspend fun clearAll(userId: String)
}