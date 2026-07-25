package com.example.sepotify.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sepotify.data.repository.ProfileRepository
import com.example.sepotify.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class SplashUiState {
    object Loading: SplashUiState()
    data class Authenticated(var userId: String): SplashUiState()
    object Unauthenticated: SplashUiState()
}

class SplashViewModel(
    private val profileRepo: ProfileRepository
): ViewModel() {

    private val _splashUiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val splashUiState: StateFlow<SplashUiState> = _splashUiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {

            when (val result = profileRepo.getCurrentUserIdOrNull()) {

                is Result.Success -> {

                    val userId = result.data

                    if (userId == null) {
                        _splashUiState.value =
                            SplashUiState.Unauthenticated
                        return@launch
                    }


                    when (profileRepo.getProfile(userId)) {

                        is Result.Success -> {
                            _splashUiState.value =
                                SplashUiState.Authenticated(userId)
                        }

                        is Result.Error -> {

                            // User exists in local session
                            // but profile/auth user is gone
                            profileRepo.signOut()

                            _splashUiState.value =
                                SplashUiState.Unauthenticated
                        }
                    }
                }


                is Result.Error -> {
                    _splashUiState.value =
                        SplashUiState.Unauthenticated
                }
            }
        }
    }

}