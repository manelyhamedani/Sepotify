package com.example.sepotify.data.local.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sepotify.data.local.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Update
    suspend fun update(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE (senderId = :userId AND recipientId = :otherId) OR (senderId = :otherId AND recipientId = :userId) ORDER BY sentAt DESC")
    fun getMessagesWith(userId: String, otherId: String): PagingSource<Int, MessageEntity>

    @Query("UPDATE messages SET readAt = :time WHERE id = :messageId")
    suspend fun markAsRead(messageId: Long, time: Instant)

    @Query("UPDATE messages SET readAt = :time WHERE senderId = :senderId AND recipientId = :recipientId")
    suspend fun markMessagesAsRead(senderId: String, recipientId: String, time: Instant)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("DELETE FROM messages WHERE (senderId = :userId AND recipientId = :otherId) OR (senderId = :otherId AND recipientId = :userId)")
    suspend fun deleteConversation(userId: String, otherId: String)

    @Query("SELECT DISTINCT CASE WHEN senderId = :userId THEN recipientId ELSE senderId END AS otherUserId FROM messages WHERE senderId = :userId OR recipientId = :userId")
    suspend fun getDistinctOtherUsers(userId: String): List<String>

    @Query("SELECT * FROM messages WHERE (senderId = :userId AND recipientId = :otherId) OR (senderId = :otherId AND recipientId = :userId) ORDER BY sentAt DESC LIMIT 1")
    suspend fun getLastMessageBetween(userId: String, otherId: String): MessageEntity?

    @Query("SELECT COUNT(*) FROM messages WHERE senderId = :otherId AND recipientId = :userId AND readAt IS NULL")
    suspend fun getUnreadCountBetween(userId: String, otherId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(map: List<MessageEntity>)
}