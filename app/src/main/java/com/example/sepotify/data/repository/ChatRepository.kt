package com.example.sepotify.data.repository

import androidx.paging.PagingData
import com.example.sepotify.data.remote.repository.SyncState
import com.example.sepotify.domain.model.Message
import com.example.sepotify.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    val syncState: StateFlow<SyncState>
    fun setCurrentChatPartner(userId: String?)
    suspend fun retrySync(userId: String)
    suspend fun sendMessage(message: Message): Result<Message>
    suspend fun markAsRead(messageId: Long): Result<Unit>
    suspend fun markMessagesAsRead(senderId: String, recipientId: String): Result<Unit>
    fun getMessagesWith(userId: String, otherId: String): Flow<PagingData<Message>>
    suspend fun getUnreadCount(userId: String): Result<Int>
    suspend fun setTyping(senderId: String, recipientId: String, isTyping: Boolean): Result<Unit>
    fun getTypingStatus(otherUserId: String, currentUserId: String): Flow<Boolean>
    suspend fun startMessageRealtime(userId: String)
    suspend fun stopMessageRealtime()
    suspend fun startTypingRealtime(senderId: String, recipientId: String)
    suspend fun stopTypingRealtime()
    suspend fun deleteMessage(messageId: Long): Result<Unit>
    suspend fun deleteConversation(currentUserId: String, otherUserId: String): Result<Unit>
    suspend fun syncMessages(userId: String): Result<Unit>
}