package com.example.sepotify.data.remote.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.sepotify.data.local.database.dao.FollowDao
import com.example.sepotify.data.local.database.entity.FollowEntity
import com.example.sepotify.data.paging.SupabasePagingSource
import com.example.sepotify.data.remote.Supabase
import com.example.sepotify.data.remote.utils.runResult
import com.example.sepotify.data.repository.FollowRepository
import com.example.sepotify.domain.model.Follow
import com.example.sepotify.domain.model.Playlist
import com.example.sepotify.domain.model.Profile
import com.example.sepotify.utils.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class SupabaseFollowRepository(
    private val client: SupabaseClient = Supabase.client,
    private val followDao: FollowDao
): FollowRepository {

    override suspend fun syncFollows(userId: String) {
        return withContext(Dispatchers.IO) {
            try {
                followDao.deleteUserFollows(userId)
                val follows = client.postgrest["follows"]
                    .select {
                        filter {
                            or {
                                eq("follower_id", userId)
                                eq("followed_id", userId)
                            }
                        }
                    }
                    .decodeList<FollowEntity>()
                followDao.insertAll(follows)
                Log.d("Supabase", "has synced follow successfully")
            } catch (e: Exception) {
                Log.d("Supabase", "Sync follow failed")
                Result.Error(e.message ?: "Sync follow failed")
            }
        }
    }

    override suspend fun follow(
        followerId: String,
        followedId: String
    ): Result<Unit> = runResult("Failed to follow") {
        val result = client.postgrest["follows"].insert(
            mapOf("follower_id" to followerId, "followed_id" to followedId)
        ) {
            select() // returns the new row
        }.decodeSingle<FollowEntity>()
        followDao.insert(result)
    }

    override suspend fun unfollow(
        followerId: String,
        followedId: String
    ): Result<Unit> = runResult("Failed to unfollow") {
        client.postgrest["follows"].delete {
            filter {
                eq("follower_id", followerId)
                eq("followed_id", followedId)
            }
        }
        followDao.delete(followerId, followedId)
    }

    override suspend fun isFollowing(
        followerId: String,
        followedId: String
    ): Result<Boolean> = runResult("Failed to check follow status") {
        client.postgrest["follows"].select {
            filter {
                eq("follower_id", followerId)
                eq("followed_id", followedId)
            }
        }
            .decodeList<Map<String, Any>>().isNotEmpty()
    }

    override fun getFollowing(userId: String): Flow<PagingData<Profile>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SupabasePagingSource { from, to ->
                    Log.d("FollowRepo", "getFollowing userId=$userId, range=$from-$to")
                    val response = client.postgrest["following_profiles"]
                        .select() {
                            filter { eq("follower_id", userId) }
                            order("follow_created_at", Order.DESCENDING)
                            range(from, to)
                        }
                    Log.d("FollowRepo", "Raw response: $response")
                    val data = response.decodeList<Profile>()
                    Log.d("FollowRepo", "getFollowing returned ${data.size} profiles: $data")
                    data
                }
            }
        ).flow

    override fun getFollowers(userId: String): Flow<PagingData<Profile>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SupabasePagingSource { from, to ->
                    Log.d("FollowRepo", "getFollowers userId=$userId, range=$from-$to")
                    val response = client.postgrest["follower_profiles"]
                        .select() {
                            filter { eq("followed_id", userId) }
                            order("follow_created_at", Order.DESCENDING)
                            range(from, to)
                        }
                    Log.d("FollowRepo", "Raw response: $response")
                    val data = response.decodeList<Profile>()
                    Log.d("FollowRepo", "getFollowers returned ${data.size} profiles: $data")
                    data
                }
            }
        ).flow

    override suspend fun getFollowingCount(userId: String): Result<Int> =
        try {
            val count = client.postgrest["follows"]
                .select(columns = Columns.raw("id")) {
                    filter { eq("follower_id", userId) }
                }
                .decodeList<Map<String, Any>>()
                .size
            Log.d("FollowRepo", "getFollowingsCount for $userId = $count")
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to count following")
        }

    override suspend fun getFollowersCount(userId: String): Result<Int> =
        try {
            val count = client.postgrest["follows"]
                .select(columns = Columns.raw("id")) {
                    filter { eq("followed_id", userId) }
                }
                .decodeList<Map<String, Any>>()
                .size
            Log.d("FollowRepo", "getFollowersCount for $userId = $count")
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to count followers")
        }


    override fun observeIsFollowing(followerId: String, followedId: String): Flow<Boolean> =
        followDao.isFollowingFlow(followerId, followedId)

    override fun observeFollowersCount(userId: String): Flow<Int> =
        followDao.getFollowersCountFlow(userId)

    override fun observeFollowingCount(userId: String): Flow<Int> =
        followDao.getFollowingCountFlow(userId)
}