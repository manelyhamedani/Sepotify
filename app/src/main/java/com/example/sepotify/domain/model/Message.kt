package com.example.sepotify.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock


@Serializable
data class Message(
    val id: Long = 0,
    @SerialName("sender_id")
    val senderId: String = "",
    @SerialName("recipient_id")
    val recipientId: String = "",
    val content: String? = null,
    @SerialName("song_id")
    val songId: String? = null,
    @SerialName("sent_at")
    val sentAt: Instant = Clock.System.now(),
    @SerialName("read_at")
    val readAt: Instant? = null,
    @SerialName("is_delivered")
    val isDelivered: Boolean = false
){
    enum class Status {
        SENDING, SENT, READ
    }

    fun getStatus(): Status = when {
        readAt != null -> Status.READ
        isDelivered -> Status.SENT
        else -> Status.SENDING
    }

}

