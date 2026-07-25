package com.example.sepotify.data.repository

import android.content.Context
import android.net.Uri
import androidx.paging.PagingData
import com.example.sepotify.domain.model.Profile
import com.example.sepotify.utils.Result
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getProfile(userId: String): Result<Profile>
    suspend fun updateProfile(profile: Profile): Result<Profile>
    suspend fun signUP(
        email: String,
        password: String,
        profile: Profile,
        imageUri: Uri?,
        context: Context
    ): Result<Profile>

    suspend fun signIn(email: String, password: String): Result<Profile>
    suspend fun signOut(): Result<Unit>
    suspend fun deleteProfile(): Result<Unit>
    suspend fun getCurrentUserIdOrNull(): Result<String?>
    suspend fun uploadAvatar(userId: String, uri: Uri, context: Context): Result<String>
    fun searchProfiles(query: String): Flow<PagingData<Profile>>
    suspend fun searchProfilesLimited(query: String, limit: Long = 4): Result<List<Profile>>
    suspend fun upgradeToPremium(userId: String): Result<Unit>

}