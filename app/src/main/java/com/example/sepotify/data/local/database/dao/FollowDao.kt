package com.example.sepotify.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.sepotify.data.local.database.entity.FollowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(follows: List<FollowEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(follow: FollowEntity)

    @Query("DELETE FROM follows")
    suspend fun clearAll()

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followedId = :followedId")
    suspend fun delete(followerId: String, followedId: String)

    @Query("SELECT * FROM follows WHERE followerId = :followerId")
    suspend fun getFollowing(followerId: String): List<FollowEntity>

    @Query("SELECT * FROM follows WHERE followedId = :userId")
    suspend fun getFollowers(userId: String): List<FollowEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerId = :followerId AND followedId = :followedId)")
    fun isFollowingFlow(followerId: String, followedId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM follows WHERE followedId = :userId")
    fun getFollowersCountFlow(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :userId")
    fun getFollowingCountFlow(userId: String): Flow<Int>

    @Query("""
DELETE FROM follows
WHERE followerId = :userId
   OR followedId = :userId
""")
    suspend fun deleteUserFollows(userId: String)
}