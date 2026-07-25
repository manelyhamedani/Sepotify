// ui/follow/FollowListViewModel.kt
package com.example.sepotify.ui.follow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.sepotify.data.repository.FollowRepository
import com.example.sepotify.domain.model.Profile
import com.example.sepotify.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FollowListUiState {
    object Loading : FollowListUiState()
    data class Success(val pagingDataFlow: Flow<PagingData<Profile>>) : FollowListUiState()
    data class Error(val message: String) : FollowListUiState()
}

class FollowListViewModel(
    private val followRepo: FollowRepository,
    private val userId: String,
    private val currentUserId: String,
    private val mode: FollowListMode
) : ViewModel() {

    enum class FollowListMode { FOLLOWING, FOLLOWERS }

    private val _uiState = MutableStateFlow<FollowListUiState>(FollowListUiState.Loading)
    val uiState: StateFlow<FollowListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        _uiState.value = FollowListUiState.Loading
        val flow = when (mode) {
            FollowListMode.FOLLOWING -> followRepo.getFollowing(userId)
            FollowListMode.FOLLOWERS -> followRepo.getFollowers(userId)
        }
        _uiState.value = FollowListUiState.Success(flow)
    }

    fun unfollow(followedId: String) {
        viewModelScope.launch {
            val result = followRepo.unfollow(userId, followedId)
            if (result is Result.Success) {
                // Refresh the list
                load()
            }
        }
    }
}