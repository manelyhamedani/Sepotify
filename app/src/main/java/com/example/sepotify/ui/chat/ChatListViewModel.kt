package com.example.sepotify.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sepotify.data.local.database.dao.MessageDao
import com.example.sepotify.data.remote.repository.SupabaseChatRepository
import com.example.sepotify.data.remote.repository.SyncState
import com.example.sepotify.data.repository.ChatRepository
import com.example.sepotify.data.repository.ProfileRepository
import com.example.sepotify.domain.model.Message
import com.example.sepotify.domain.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.sepotify.utils.Result
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock

data class Conversation(
    val otherUserId: String,
    val otherProfile: Profile?,
    val lastMessage: Message?,
    val lastMessageTime: Instant,
    val unreadCount: Int
)

sealed class ChatListUiState {
    object Loading : ChatListUiState()
    data class Success(val conversations: List<Conversation>) : ChatListUiState()
    data class Error(val message: String) : ChatListUiState()
}

class ChatListViewModel(
    private val messageDao: MessageDao,
    private val chatRepo: ChatRepository,
    private val profileRepo: ProfileRepository,
    private val userId: String
): ViewModel() {
    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Loading)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var wasLoaded = false

    init {
        viewModelScope.launch {
            chatRepo.syncState.collect { state ->
                _syncState.value = state
                if (state is SyncState.Done && !wasLoaded) {
                    wasLoaded = true
                    loadConversations()
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadConversations()
        }
    }

    private fun loadConversations() {
        viewModelScope.launch {
            _uiState.value = ChatListUiState.Loading

            try {
                val otherUserIds = messageDao.getDistinctOtherUsers(userId)
                val conversations = mutableListOf<Conversation>()
                for (otherId in otherUserIds) {
                    val profileResult = profileRepo.getProfile(otherId)
                    val profile = if (profileResult is Result.Success) profileResult.data else null
                    val lastMsg = messageDao.getLastMessageBetween(userId, otherId)
                    val unreadCount = messageDao.getUnreadCountBetween(userId, otherId)
                    conversations.add(
                        Conversation(
                            otherUserId = otherId,
                            otherProfile = profile,
                            lastMessage = lastMsg?.toMessage(),
                            lastMessageTime = lastMsg?.sentAt ?: Clock.System.now(),
                            unreadCount = unreadCount
                        )
                    )
                }

                conversations.sortByDescending { it.lastMessageTime }
                _uiState.value = ChatListUiState.Success(conversations)
            } catch (e: Exception) {
                _uiState.value = ChatListUiState.Error(e.message ?: "Failed to load conversations")
            }
        }
    }

    fun retrySync() {
        viewModelScope.launch {
            chatRepo.retrySync(userId)
        }
    }
}
