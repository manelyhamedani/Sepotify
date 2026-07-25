package com.example.sepotify.data.repository

import androidx.paging.PagingData
import com.example.sepotify.domain.model.Follow
import com.example.sepotify.domain.model.Profile
import com.example.sepotify.utils.Result
import kotlinx.coroutines.flow.Flow

interface FollowRepository {
    suspend fun follow(followerId: String, followedId: String): Result<Unit>
    suspend fun unfollow(followerId: String, followedId: String): Result<Unit>
    suspend fun isFollowing(followerId: String, followedId: String): Result<Boolean>
    fun getFollowing(userId: String): Flow<PagingData<Profile>>
    fun getFollowers(userId: String): Flow<PagingData<Profile>>
    suspend fun getFollowingCount(userId: String): Result<Int>
    suspend fun getFollowersCount(userId: String): Result<Int>
    suspend fun syncFollows(userId: String)
    fun observeIsFollowing(followerId: String, followedId: String): Flow<Boolean>
    fun observeFollowersCount(userId: String): Flow<Int>
    fun observeFollowingCount(userId: String): Flow<Int>
}