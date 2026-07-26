package com.example.sepotify.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sepotify.data.repository.ProfileRepository
import com.example.sepotify.domain.model.Profile
import com.example.sepotify.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading: ProfileUiState()
    data class Success(var profile: Profile): ProfileUiState()
    data class Error(var message: String): ProfileUiState()
    object Deleted: ProfileUiState()
}

class ProfileViewModel(
    private val profileRepo: ProfileRepository,
    private val application: Application
): ViewModel() {

    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()
    private val _followingCount = MutableStateFlow(0)
    val followingCount: StateFlow<Int> = _followingCount.asStateFlow()

    private val _followersCount = MutableStateFlow(0)
    val followersCount: StateFlow<Int> = _followersCount.asStateFlow()

    var currentProfileId: String? = null

    fun myProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            when (val result = profileRepo.getCurrentUserIdOrNull()) {
                is Result.Success -> {
                    if (result.data != null) {
                        currentProfileId = result.data
                        loadProfile(result.data)
                    } else {
                        _profileState.value = ProfileUiState.Error("User not logged in")
                    }
                }
                is Result.Error -> _profileState.value = ProfileUiState.Error(result.message)
            }
        }
    }

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            when (val result = profileRepo.getProfile(userId)) {
                is Result.Success -> _profileState.value = ProfileUiState.Success(result.data)
                is Result.Error -> _profileState.value = ProfileUiState.Error(result.message)
            }
        }
    }

    fun updateProfile(updatedProfile: Profile) {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            when (val result = profileRepo.updateProfile(updatedProfile)) {
                is Result.Success -> _profileState.value = ProfileUiState.Success(result.data)
                is Result.Error -> _profileState.value = ProfileUiState.Error(result.message)
            }
        }
    }

    fun deleteProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            when (val result = profileRepo.deleteProfile()) {
                is Result.Success -> {
                    _profileState.value = ProfileUiState.Deleted
                    onSuccess()
                }
                is Result.Error -> _profileState.value = ProfileUiState.Error(result.message)
            }
        }
    }


    //TODO: need a full refactor
    fun upgradeToPremium() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            _profileState.value = ProfileUiState.Loading
            val result = profileRepo.upgradeToPremium(currentProfileId ?: return@launch)
            if (result is Result.Success) {
                myProfile()
            } else {
                val errorMsg = "Failed to upgrade"
                _profileState.value = ProfileUiState.Error(errorMsg)
                myProfile()
            }
        }
    }

    fun updateAvatar(newUrl: String) {
        viewModelScope.launch {
            if (_profileState.value is ProfileUiState.Success) {
                val profile = (_profileState.value as ProfileUiState.Success).profile
                val updated = profile.copy(avatarUrl = newUrl)
                profileRepo.updateProfile(updated)
                myProfile()
            }
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            val userId = currentProfileId
            if (userId == null) {
                _profileState.value = ProfileUiState.Error("User not logged in")
                return@launch
            }
            // Upload to Supabase
            when (val result = profileRepo.uploadAvatar(userId, uri, application.applicationContext)) {
                is Result.Success -> {
                    val publicUrl = result.data
                    // Update profile with new avatar URL
                    val currentState = _profileState.value
                    if (currentState is ProfileUiState.Success) {
                        val updatedProfile = currentState.profile.copy(avatarUrl = publicUrl)
                        // Update in database
                        when (val updateResult = profileRepo.updateProfile(updatedProfile)) {
                            is Result.Success -> {
                                _profileState.value = ProfileUiState.Success(updateResult.data)
                            }
                            is Result.Error -> {
                                _profileState.value = ProfileUiState.Error(updateResult.message)
                            }
                        }
                    }
                }
                is Result.Error -> {
                    _profileState.value = ProfileUiState.Error(result.message)
                }
            }
        }
    }
}