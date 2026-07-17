package com.example.sepotify.data.remote.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.sepotify.data.local.database.dao.MessageDao
import com.example.sepotify.data.local.database.entity.MessageEntity
import com.example.sepotify.data.remote.Supabase
import com.example.sepotify.data.repository.ChatRepository
import com.example.sepotify.domain.model.Message
import com.example.sepotify.utils.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class TypingEvent(
    val senderId: String,
    val recipientId: String,
    val isTyping: Boolean
)

sealed class SyncState {
    object Loading : SyncState()
    object Done : SyncState()
    data class Error(val message: String) : SyncState()
}

class SupabaseChatRepository(
    private val client: SupabaseClient = Supabase.client,
    private val messageDao: MessageDao
): ChatRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var messageListeningJob: Job? = null
    private var incomingMessagesChannel: RealtimeChannel? = null
    private var outgoingMessagesChannel: RealtimeChannel? = null
    private var typingChannel: RealtimeChannel? = null
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Loading)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    private var currentChatPartner: String? = null

    override fun setCurrentChatPartner(userId: String?) {
        currentChatPartner = userId
    }

    override suspend fun syncMessages(userId: String): Result<Unit> {
        _syncState.value = SyncState.Loading
        return withContext(Dispatchers.IO) {
            try {

                val messages = client.postgrest["messages"]
                    .select {
                        filter {
                            or {
                                eq("sender_id", userId)
                                eq("recipient_id", userId)
                            }
                        }
                    }
                    .decodeList<Message>()


                messageDao.insertAll(
                    messages.map {
                        MessageEntity(
                            id = it.id,
                            senderId = it.senderId,
                            recipientId = it.recipientId,
                            content = it.content,
                            songId = it.songId,
                            sentAt = it.sentAt,
                            readAt = it.readAt,
                            isDelivered = true
                        )
                    }
                )
                _syncState.value = SyncState.Done
                Result.Success(Unit)

            } catch(e: Exception) {
                Log.d("Supabase", "Sync failed")
                _syncState.value = SyncState.Error(e.message ?: "Sync failed")
                Result.Error(e.message ?: "Sync failed")
            }
        }
    }

    override suspend fun retrySync(userId: String) {
        syncMessages(userId)
    }

    override suspend fun startMessageRealtime(userId: String) {
        if (incomingMessagesChannel != null && outgoingMessagesChannel != null && messageListeningJob?.isActive == true) {
            Log.d("Supabase", "Realtime already active for userId: $userId")
            return
        }

        stopMessageRealtime()

        Log.d("Supabase", "Starting incoming message realtime for userId: $userId")

        incomingMessagesChannel = client.realtime.channel(
            "messages-received-$userId"
        )

        Log.d("Supabase", "Incoming channel created: messages-${userId}")

        val incomingFlow = incomingMessagesChannel!!
            .postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "messages"
                filter(
                    "recipient_id",
                    FilterOperator.EQ,
                    userId
                )
            }

        Log.d("Supabase", "postgresChangeFlow defined, about to subscribe to incoming channel")
        incomingMessagesChannel!!.subscribe(blockUntilSubscribed = true)
        Log.d("Supabase", "incoming channel Subscribed successfully")

        Log.d("Supabase", "Starting outgoing message realtime for userId: $userId")

        outgoingMessagesChannel = client.realtime.channel(
            "messages-sent-$userId"
        )

        Log.d("Supabase", "Outgoing channel created: messages-${userId}")

        val outgoingFlow = outgoingMessagesChannel!!
            .postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "messages"
                filter(
                    "sender_id",
                    FilterOperator.EQ,
                    userId
                )
            }

        Log.d("Supabase", "postgresChangeFlow defined, about to subscribe to outgoing channel")
        outgoingMessagesChannel!!.subscribe(blockUntilSubscribed = true)
        Log.d("Supabase", "outgoing channel Subscribed successfully")

        messageListeningJob = scope.launch {
            Log.d("Supabase", "Collector started")
            merge(incomingFlow, outgoingFlow).collect { action ->
                Log.d("Supabase", "Received action: $action")
                try {
                    when (action) {
                        is PostgresAction.Insert -> {
                            val entity = action.decodeRecord<MessageEntity>()
                            messageDao.insert(entity)

                            if (entity.recipientId == userId && entity.senderId == currentChatPartner) {
                                scope.launch {
                                    Log.d("Supabase", "going to read my current chat partner($currentChatPartner) message")
                                    // This will update read_at on Supabase and in Room
                                    markMessagesAsRead(entity.senderId, userId)
                                }
                            }
                        }
                        is PostgresAction.Update -> {
                            messageDao.update(
                                action.decodeRecord<MessageEntity>()
                            )
                        }
                        else -> {} //TODO: implement delete
                    }
                } catch (e: Exception) {
                    Log.e("SupabaseChatRepo", "Realtime error", e)
                }
            }
        }

    }

    override suspend fun stopMessageRealtime() {
        messageListeningJob?.cancel()
        messageListeningJob = null
        incomingMessagesChannel?.unsubscribe()
        incomingMessagesChannel = null
        outgoingMessagesChannel?.unsubscribe()
        outgoingMessagesChannel = null
    }

    fun conversationId(
        user1:String,
        user2:String
    ):String {
        return listOf(user1,user2)
            .sorted()
            .joinToString("_")
    }

    override suspend fun startTypingRealtime(senderId: String, recipientId: String) {
        stopTypingRealtime()

        try {
            val id = conversationId(senderId, recipientId)
            Log.d("Typing", "Starting typing channel: typing-$id")
            typingChannel = client.realtime.channel("typing-$id")
            typingChannel!!.subscribe(blockUntilSubscribed = true)
            Log.d("Typing", "Subscribed to typing channel successfully")
        } catch (e: Exception) {
            Log.e("SupabaseChatRepo", "Failed to start typing realtime", e)
            typingChannel = null // ensure it's null on failure
        }
    }

    override suspend fun stopTypingRealtime() {
        typingChannel?.unsubscribe()
        typingChannel = null
    }

    override suspend fun setTyping(senderId: String, recipientId: String, isTyping: Boolean): Result<Unit> {
        return try {
            val channel = typingChannel ?: return Result.Error("Typing channel not initialized")
            channel.broadcast("typing", TypingEvent(senderId, recipientId, isTyping))
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("Typing", "Failed to send typing status", e)
            Result.Error(e.message ?: "Failed to send typing status")
        }
    }

    override fun getTypingStatus(otherUserId: String, currentUserId: String): Flow<Boolean> {
        return typingChannel?.broadcastFlow<TypingEvent>("typing")
            ?.map { event ->
                event.senderId == otherUserId &&
                        event.recipientId == currentUserId &&
                        event.isTyping
            } ?: emptyFlow()
    }

    override suspend fun sendMessage(message: Message): Result<Message>  {
        return try {
            val inserted = client.postgrest["messages"].insert(
                Message(
                    senderId = message.senderId,
                    recipientId = message.recipientId,
                    content = message.content,
                    songId = message.songId,
                    sentAt = Clock.System.now(),
                    isDelivered = true
                )
            ) {
                select()
            }.decodeSingle<Message>()

            messageDao.insert(
                MessageEntity(
                    id = inserted.id,
                    senderId = inserted.senderId,
                    recipientId = inserted.recipientId,
                    content = inserted.content,
                    songId = inserted.songId,
                    sentAt = inserted.sentAt,
                    readAt = inserted.readAt,
                    isDelivered = true
                )
            )

            Result.Success(inserted)
        } catch (e: Exception) {
            val local = message.copy(
                id = System.currentTimeMillis(), // temporary id
                sentAt = Clock.System.now(),
                isDelivered = false
            )
            messageDao.insert(
                MessageEntity(
                    id = local.id,
                    senderId = local.senderId,
                    recipientId = local.recipientId,
                    content = local.content,
                    songId = local.songId,
                    sentAt = local.sentAt,
                    readAt = null,
                    isDelivered = false
                )
            )
            Log.d("Supabase", e.message ?: "failed to send")
            Result.Error(e.message ?: "Failed to send")
        }
    }

    override suspend fun markAsRead(messageId: Long): Result<Unit> {
        return try {
            val currentTime = Clock.System.now()
            client.postgrest["messages"].update(
                mapOf(
                    "read_at" to currentTime
                )
            )    {
                filter {
                    eq("id", messageId)
                    filter("read_at", FilterOperator.IS, null)
                }
            }

            messageDao.markAsRead(messageId, currentTime)

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.d("Supabase", e.message ?: "failed to mark as read")
            Result.Error(e.message ?: "Failed to mark as read")
        }
    }

    override suspend fun markMessagesAsRead(
        senderId: String,
        recipientId: String
    ): Result<Unit> {
        return try {
            val currentTime = Clock.System.now()
            client.postgrest["messages"].update(
                mapOf(
                    "read_at" to currentTime
                )
            )    {
                filter {
                    eq("sender_id", senderId)
                    eq("recipient_id", recipientId)
                    filter("read_at", FilterOperator.IS, null)
                }
            }

            messageDao.markMessagesAsRead(senderId, recipientId, currentTime)
            Log.d("Supabase", "has read")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.d("Supabase", e.message ?: "failed to mark messages as read")
            Result.Error(e.message ?: "Failed to mark messages as read")
        }
    }

    override fun getMessagesWith(
        userId: String,
        otherId: String
    ): Flow<PagingData<Message>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                messageDao.getMessagesWith(userId, otherId)
            }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                entity.toMessage()
            }
        }

    override suspend fun getUnreadCount(userId: String): Result<Int> {
        return try {
            val count = client.postgrest["messages"]
                .select(columns = Columns.raw("id")) {
                    filter {
                        eq("recipient_id", userId)
                        filter("read_at", FilterOperator.IS, null)
                    }
                }
                .decodeList<Map<String, Any>>()
                .size
            Result.Success(count)
        } catch (e: Exception) {
            Log.d("Supabase", e.message ?: "failed to get unread count")
            Result.Error(e.message ?: "Failed to get unread count")
        }
    }

    override suspend fun deleteMessage(messageId: Long): Result<Unit> {
        return try {
            messageDao.deleteMessage(messageId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.d("Supabase", e.message ?: "failed to delete message")
            Result.Error(e.message ?: "Failed to delete message")
        }
    }

    override suspend fun deleteConversation(currentUserId: String, otherUserId: String): Result<Unit> {
        return try {
            client.postgrest["messages"]
                .delete {
                    filter {
                        or {
                            and {
                                eq("sender_id", currentUserId)
                                eq("recipient_id", otherUserId)
                            }
                            and {
                                eq("sender_id", otherUserId)
                                eq("recipient_id", currentUserId)
                            }
                        }
                    }
                }
            messageDao.deleteConversation(currentUserId, otherUserId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.d("Supabase", e.message ?: "Failed to delete conversation")
            Result.Error(e.message ?: "Failed to delete conversation")
        }
    }
}