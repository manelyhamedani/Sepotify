package com.example.sepotify.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sepotify.domain.model.Message
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Long = 0,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("recipient_id")
    val recipientId: String,
    val content: String?,
    @SerialName("song_id")
    val songId: String?,
    @SerialName("sent_at")
    val sentAt: Instant,
    @SerialName("read_at")
    val readAt: Instant? = null,
    @SerialName("is_delivered")
    val isDelivered: Boolean = false
) {
    fun toMessage(): Message {
        return Message(
            id = id,
            senderId = senderId,
            recipientId = recipientId,
            content = content,
            songId = songId,
            sentAt = sentAt,
            readAt = readAt,
            isDelivered = isDelivered
        )
    }

}