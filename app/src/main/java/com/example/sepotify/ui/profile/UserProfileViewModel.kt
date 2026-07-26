package com.example.sepotify.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.sepotify.data.repository.FollowRepository
import com.example.sepotify.data.repository.MusicRepository
import com.example.sepotify.data.repository.ProfileRepository
import com.example.sepotify.domain.model.Playlist
import com.example.sepotify.domain.model.Profile
import com.example.sepotify.utils.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class UserProfileUiState {
    object Loading : UserProfileUiState()
    data class Success(val profile: Profile) : UserProfileUiState()
    data class Error(val message: String) : UserProfileUiState()
}

data class FollowState(
    val isFollowing: Boolean = false,
    val isLoading: Boolean = false
)

class UserProfileViewModel(
    private val profileRepo: ProfileRepository,
    private val followRepo: FollowRepository,
    private val musicRepo: MusicRepository,
    private val userId: String,
    private val currentUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val _followState = MutableStateFlow(FollowState())
    val followState: StateFlow<FollowState> = _followState.asStateFlow()

    private val _followingCount = MutableStateFlow(0)
    val followingCount: StateFlow<Int> = _followingCount.asStateFlow()

    private val _followersCount = MutableStateFlow(0)
    val followersCount: StateFlow<Int> = _followersCount.asStateFlow()

    val publicPlaylistsFlow: Flow<PagingData<Playlist>> = musicRepo.getPublicUserPlaylists(userId)

    fun syncFollows() {
        viewModelScope.launch {
            followRepo.syncFollows(userId)
        }
    }

    init {
        loadProfile(userId)

        // Observe follow status from Room (real‑time)
        viewModelScope.launch {
            followRepo.observeIsFollowing(currentUserId, userId)
                .collect { isFollowing ->
                    _followState.value = _followState.value.copy(
                        isFollowing = isFollowing,
                        isLoading = false
                    )
                }
        }

        // Observe counts from Room (updates after sync and follow/unfollow)
        viewModelScope.launch {
            followRepo.observeFollowersCount(userId)
                .collect { count ->
                    Log.d("FOLLOW", "followers Flow = $count")
                    _followersCount.value = count
                }
        }
        viewModelScope.launch {
            followRepo.observeFollowingCount(userId)
                .collect { count ->
                    Log.d("FOLLOW", "followersings Flow = $count")
                    _followingCount.value = count
                }
        }

    }

    private fun loadProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading
            when (val result = profileRepo.getProfile(userId)) {
                is Result.Success -> _uiState.value = UserProfileUiState.Success(result.data)
                is Result.Error -> _uiState.value = UserProfileUiState.Error(result.message)
            }
        }
    }

    fun follow(userId: String, currentUserId: String) {
        if (userId == currentUserId) return
        viewModelScope.launch {
            _followState.value = _followState.value.copy(isLoading = true)
            val result = followRepo.follow(currentUserId, userId)
            if (result is Result.Success) {
                _followState.value = _followState.value.copy(isFollowing = true, isLoading = false)
            } else {
                _followState.value = _followState.value.copy(isLoading = false)
            }
        }
    }

    fun unfollow(userId: String, currentUserId: String) {
        if (userId == currentUserId) return
        viewModelScope.launch {
            _followState.value = _followState.value.copy(isLoading = true)
            val result = followRepo.unfollow(currentUserId, userId)
            if (result is Result.Success) {
                _followState.value = _followState.value.copy(isFollowing = false, isLoading = false)
            } else {
                _followState.value = _followState.value.copy(isLoading = false)
            }
        }
    }
}