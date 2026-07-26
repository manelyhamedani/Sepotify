package com.example.sepotify.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.sepotify.data.remote.repository.SyncState
import com.example.sepotify.data.repository.ChatRepository
import com.example.sepotify.data.repository.ProfileRepository
import com.example.sepotify.domain.model.Message
import com.example.sepotify.domain.model.Profile
import com.example.sepotify.utils.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ChatViewModel(
    private val chatRepo: ChatRepository,
    private val profileRepo: ProfileRepository,
    private val currentUserId: String,
    private val otherUserId: String
) : ViewModel() {

    val messageFlow: Flow<PagingData<Message>> = chatRepo.getMessagesWith(currentUserId, otherUserId)

    private val _partnerProfile = MutableStateFlow<Profile?>(null)
    val partnerProfile: StateFlow<Profile?> = _partnerProfile.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Loading)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        viewModelScope.launch {
            // Load partner profile
            when (val result = profileRepo.getProfile(otherUserId)) {
                is Result.Success -> _partnerProfile.value = result.data
                else -> _partnerProfile.value = null // fallback
            }
        }

        viewModelScope.launch {
            chatRepo.syncState.collect { state ->
                _syncState.value = state
            }
        }

        viewModelScope.launch {
            // Wait for sync to be done first (optional)
            chatRepo.syncState.first { it is SyncState.Done }
            chatRepo.startTypingRealtime(currentUserId, otherUserId)
            chatRepo.getTypingStatus(otherUserId, currentUserId)
                .collect { typing ->
                    _isTyping.value = typing
                }
        }

        chatRepo.setCurrentChatPartner(otherUserId)
    }

    override fun onCleared() {
        viewModelScope.launch {
            chatRepo.setCurrentChatPartner(null)
            chatRepo.stopTypingRealtime()
        }
        super.onCleared()
    }

    fun retrySync() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            val result = chatRepo.syncMessages(currentUserId)
            _syncState.value = if (result is Result.Success) {
                chatRepo.startMessageRealtime(currentUserId)
                SyncState.Done
            } else {
                SyncState.Error("Unknown error")
            }
        }
    }

    fun sendMessage(text: String, songId: String? = null) {
        if (text.isBlank() && songId == null) return
        viewModelScope.launch {
            _isSending.value = true
            val message = Message(
                senderId = currentUserId,
                recipientId = otherUserId,
                content = text.takeIf { it.isNotBlank() },
                songId = songId,
                sentAt = Clock.System.now()
            )
            chatRepo.sendMessage(message)
            _isSending.value = false
        }
    }

    fun sendSong(songId: String) {
        sendMessage("", songId)
    }

    fun setTyping(isTyping: Boolean) {
        viewModelScope.launch {
            chatRepo.setTyping(currentUserId, otherUserId, isTyping)
        }
    }

    fun retrySending(message: Message) {
        viewModelScope.launch {
            chatRepo.sendMessage(message)
        }
    }

    fun markMessagesAsRead() {
        viewModelScope.launch {
            chatRepo.markMessagesAsRead(otherUserId, currentUserId)
        }
    }

    fun clearChatPartner() {
        chatRepo.setCurrentChatPartner(null)
    }

    fun deleteConversation(onSuccess: () -> Unit) {
        viewModelScope.launch {
            chatRepo.deleteConversation(currentUserId, otherUserId)
            onSuccess()
        }
    }
}