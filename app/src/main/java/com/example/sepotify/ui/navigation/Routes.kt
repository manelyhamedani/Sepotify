package com.example.sepotify.ui.navigation

import kotlinx.serialization.Serializable

sealed class Routes {

    // ---------- Auth ----------
    @Serializable
    object Splash : Routes()

    @Serializable
    object SignIn : Routes()

    @Serializable
    object SignUpStep1 : Routes()

    @Serializable
    object SignUpStep2 : Routes()


    // ---------- Main Graph ----------
    @Serializable
    data class Main(val userId: String) : Routes()


    // ---------- Bottom Navigation ----------
    @Serializable
    object Home : Routes()

    @Serializable
    object Search : Routes()

    @Serializable
    object Library : Routes()

    @Serializable
    object Profile : Routes()


    // ---------- Other Screens ----------
    @Serializable
    data class UserProfile(val userId: String)

    @Serializable
    data class ArtistDetail(val artistId: String)

    @Serializable
    data class PlaylistDetail(val playlistId: String)
    @Serializable
    data class Player(val userId: String) : Routes()
    @Serializable
    object ChatList
    @Serializable
    object Settings : Routes()
    @Serializable
    object Downloads : Routes()

    @Serializable
    data class FollowList(val userId: String, val currentUserId: String, val mode: String)

    @Serializable
    object RecentlyPlayed: Routes()

    @Serializable
    object TopArtists: Routes()

    @Serializable
    object LikedSongs : Routes()
    @Serializable
    data class Chat(val currentUserId: String, val otherUserId: String)
}