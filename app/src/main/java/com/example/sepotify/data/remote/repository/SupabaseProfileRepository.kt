package com.example.sepotify.data.remote.repository

import android.content.Context
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.sepotify.data.paging.SupabasePagingSource
import com.example.sepotify.data.remote.Supabase
import com.example.sepotify.data.repository.ProfileRepository
import com.example.sepotify.domain.model.Profile
import com.example.sepotify.utils.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseProfileRepository(
    private val client: SupabaseClient = Supabase.client
): ProfileRepository {

    override suspend fun getProfile(userId: String): Result<Profile> {
        return try {
            val profile = client.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<Profile>()
            Result.Success(profile)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get profile")
        }
    }

    override suspend fun signUP(email: String, password: String, profile: Profile, imageUri: Uri?, context: Context): Result<Profile> {
        return try {
            val signUpResult = client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("username", profile.username)
                    put("full_name", profile.fullName)
                    put("avatar_url", profile.avatarUrl)
                    put("is_premium", profile.isPremium)
                }
            }
            val userId = client.auth.currentUserOrNull()?.id ?: return Result.Error("User created but session not available")
//            val userId = signUpResult?.id ?: return Result.Error("Sign-up succeeded but user ID not returned")
            if (imageUri != null) {
                when (val avatarUrl = uploadAvatar(userId, imageUri, context)) {
                    is Result.Success -> {
                        val updatedProfile = profile.copy(id = userId, avatarUrl = avatarUrl.data)
                        updateProfile(updatedProfile)
                    }

                    is Result.Error -> return Result.Error(avatarUrl.message)
                }

            }
            getProfile(userId)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Sign-up Failed")
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Profile> {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = client.auth.currentUserOrNull()?.id ?: return Result.Error("Sign-in succeeded but user ID not found")
            getProfile(userId)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Sign-in Failed")
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            client.auth.signOut(SignOutScope.GLOBAL)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Sign-out Failed")
        }
    }

    override suspend fun deleteProfile(): Result<Unit> {
//        return try {
//            val userId = client.auth.currentUserOrNull()?.id ?: return Result.Error("No authenticated user")
//
//            val bucket = client.storage["assets"]
//            val files = bucket.list("users/$userId")
//            for (file in files) {
//                bucket.delete("users/$userId/${file.name}")
//            }
//            client.auth.admin.deleteUser(userId)
//            Result.Success(Unit)
//        } catch (e: Exception) {
//            Result.Error(e.message ?: "Failed to delete profile")
//        }
        TODO("handle with edge function")
    }

    override suspend fun updateProfile(profile: Profile): Result<Profile> {
        return try {
            client.postgrest["profiles"]
                .update(profile) {
                    filter {
                        eq("id", profile.id)
                    }
                }
            Result.Success(profile)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update profile")
        }
    }

    override suspend fun getCurrentUserIdOrNull(): Result<String?> {
        return try {
            val currentUserId = client.auth.currentUserOrNull()?.id
            Result.Success(currentUserId)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch current user")
        }
    }

    override suspend fun uploadAvatar(userId: String, uri: Uri, context: Context): Result<String> {
        return try {
            val bytes = context.contentResolver
                .openInputStream(uri)
                ?.readBytes()
                ?: return Result.Error("Cannot read image")

            val fileName = "users/$userId/avatar_${System.currentTimeMillis()}.jpg"

            val bucket = client.storage["assets"]

            bucket.upload(
                path = fileName,
                data = bytes,
                options = {
                    upsert = true
                }
            )

            val publicUrl = bucket.publicUrl(fileName)

            Result.Success(publicUrl)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Upload failed")
        }
    }

    override fun searchProfiles(query: String): Flow<PagingData<Profile>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SupabasePagingSource { from, to ->
                    client.postgrest["profiles"]
                        .select {
                            filter {
                                or {
                                    ilike("username", "%$query%")
                                    ilike("full_name", "%$query%")
                                }
                            }
                            range(from, to)
                        }
                        .decodeList<Profile>()
                }
            }
        ).flow

    // SupabaseProfileRepository.kt
    override suspend fun searchProfilesLimited(query: String, limit: Long): Result<List<Profile>> =
        try {
            val profiles = client.postgrest["profiles"]
                .select {
                    filter {
                        or {
                            ilike("username", "%$query%")
                            ilike("full_name", "%$query%")
                        }
                    }
                    limit(limit)
                }
                .decodeList<Profile>()
            Result.Success(profiles)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to search profiles")
        }

    override suspend fun upgradeToPremium(userId: String): Result<Unit> {
        return try {
            client.postgrest["profiles"]
                .update(mapOf("is_premium" to true)) {
                    filter { eq("id", userId) }
                }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to upgrade")
        }
    }

}

