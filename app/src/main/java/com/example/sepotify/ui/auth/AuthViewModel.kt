package com.example.sepotify.ui.auth

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
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.sepotify.R
import com.example.sepotify.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class ErrorType {
    VALIDATION,   // client-side validation (show inline)
    AUTHENTICATION,  // wrong credentials, user not found (inline)
    NETWORK,      // network/connection issues (show snackbar)
    GENERAL       // other backend errors (show snackbar)
}

sealed class AuthState {
    object Idle: AuthState()
    object Loading: AuthState()
    data class SignUpStep1(val email: String, val password: String): AuthState()
    data class Authenticated(val profile: Profile): AuthState()
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.GENERAL
    ): AuthState()
}

sealed class AuthEffect {
    data class ShowSnackbar(val message: String) : AuthEffect()
    // future: NavigateToHome, NavigateToProfile, etc.
}

class AuthViewModel(
    private val profileRepo: ProfileRepository,
    private val application: Application,
) : AndroidViewModel(application) {

    // --- State (persistent) ---
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // --- Effects (one-time events) ---
    private val _effect = MutableSharedFlow<AuthEffect>()
    val effect: SharedFlow<AuthEffect> = _effect.asSharedFlow()

    private var tempSignUpEmail: String? = null
    private var tempSignUpPassword: String? = null


    // --- Validation helpers ---
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }
    private fun isValidUsername(username: String): Boolean = username.length >= 8
    private fun isValidPassword(password: String): Boolean = password.length >= 8
    private fun isValidFullName(fullName: String): Boolean = fullName.isNotBlank()

    // --- Sign In ---
    fun signIn(email: String, password: String) {
        if (!isValidEmail(email)) {
            _authState.value = AuthState.Error(
                message = application.getString(R.string.auth_error_invalid_email),
                type = ErrorType.VALIDATION
            )
            return
        }
        if (!isValidPassword(password)) {
            _authState.value = AuthState.Error(
                message = application.getString(R.string.auth_error_invalid_password),
                type = ErrorType.VALIDATION
            )
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = profileRepo.signIn(email, password)) {
                is Result.Success -> {
                    _authState.value = AuthState.Authenticated(result.data)
                }
                is Result.Error -> {
                    handleError(result.message)
                }
            }
        }
    }

    // --- Sign Up Step 1 ---
    fun submitEmailPassword(email: String, password: String) {
        if (!isValidEmail(email)) {
            _authState.value = AuthState.Error(
                message = application.getString(R.string.auth_error_invalid_email),
                type = ErrorType.VALIDATION
            )
            return
        }
        if (!isValidPassword(password)) {
            _authState.value = AuthState.Error(
                message = application.getString(R.string.auth_error_invalid_password),
                type = ErrorType.VALIDATION
            )
            return
        }
        tempSignUpEmail = email
        tempSignUpPassword = password
        _authState.value = AuthState.SignUpStep1(email, password)
    }

    // --- Sign Up Step 2 (complete) ---
    fun completeSignUp(username: String, fullName: String, imageUri: Uri?) {
        if (!isValidUsername(username)) {
            _authState.value = AuthState.Error(
                message = application.getString(R.string.auth_error_invalid_username),
                type = ErrorType.VALIDATION
            )
            return
        }
        if (!isValidFullName(fullName)) {
            _authState.value = AuthState.Error(
                message = application.getString(R.string.auth_error_invalid_fullname),
                type = ErrorType.VALIDATION
            )
            return
        }
        val email = tempSignUpEmail
        val password = tempSignUpPassword
        if (email == null || password == null) {
            _authState.value = AuthState.Error(
                message = application.getString(R.string.auth_error_step_mismatch),
                type = ErrorType.VALIDATION
            )
            return
        }
        val profile = Profile(
            id = "",
            username = username,
            fullName = fullName,
            avatarUrl = "",
            isPremium = false
        )

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = profileRepo.signUP(email, password, profile, imageUri, application.applicationContext)) {
                is Result.Success -> {
                    tempSignUpEmail = null
                    tempSignUpPassword = null
                    _authState.value = AuthState.Authenticated(result.data)
                }
                is Result.Error -> {
                    handleError(result.message)
                    Log.e("AuthViewModel", "Sign-up error: ${result.message}")
                }
            }
        }
    }

    // --- Log Out ---
    fun logOut() {
        tempSignUpEmail = null
        tempSignUpPassword = null
        viewModelScope.launch {
            when (val result = profileRepo.signOut()) {
                is Result.Success -> {
                    _authState.value = AuthState.Idle
                }
                is Result.Error -> {
                    handleError(result.message)
                }
            }
        }
    }

    // --- Clear any error (called when user types) ---
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    // --- Centralized error handling ---
    private suspend fun handleError(message: String) {
        val mapped = mapResultError(message)
        when (mapped.type) {
            ErrorType.VALIDATION, ErrorType.AUTHENTICATION -> {
                // Show inline
                _authState.value = mapped
            }
            ErrorType.NETWORK, ErrorType.GENERAL -> {
                // Emit one-time snackbar event
                _effect.emit(AuthEffect.ShowSnackbar(mapped.message))
                // Reset state to Idle so the button is re‑enabled
                _authState.value = AuthState.Idle
            }
        }
    }

    // --- Map repository error to AuthState.Error with appropriate type ---
    private fun mapResultError(message: String): AuthState.Error {
        val lowerMsg = message.lowercase()
        return when {
            // Network issues
            lowerMsg.contains("network") ||
                    lowerMsg.contains("connection") ||
                    lowerMsg.contains("timeout") ||
                    lowerMsg.contains("internet") ||
                    lowerMsg.contains("unable to connect") ||
                    lowerMsg.contains("host") -> {
                AuthState.Error(
                    message = application.getString(R.string.auth_error_network_generic),
                    type = ErrorType.NETWORK
                )
            }
            // Authentication failures
            lowerMsg.contains("not found") ||
                    lowerMsg.contains("invalid") ||
                    lowerMsg.contains("incorrect") ||
                    lowerMsg.contains("credentials") ||
                    lowerMsg.contains("password") ||
                    (lowerMsg.contains("user") && (lowerMsg.contains("not exist") || lowerMsg.contains("does not exist"))) -> {
                AuthState.Error(
                    message = application.getString(R.string.auth_error_user_not_exists),
                    type = ErrorType.AUTHENTICATION
                )
            }
            // Other backend errors
            else -> {
                AuthState.Error(
                    message = message,
                    type = ErrorType.GENERAL
                )
            }
        }
    }
}