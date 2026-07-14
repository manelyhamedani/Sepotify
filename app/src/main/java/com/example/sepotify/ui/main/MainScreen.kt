package com.example.sepotify.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.sepotify.data.player.PlayerManager
import com.example.sepotify.data.repository.ChatRepository
import com.example.sepotify.data.repository.FollowRepository
import com.example.sepotify.ui.artist.ArtistDetailScreen
import com.example.sepotify.ui.chat.ChatScreen
import com.example.sepotify.ui.home.HomeScreen
import com.example.sepotify.ui.navigation.Routes
import com.example.sepotify.ui.player.PlayerBar
import com.example.sepotify.ui.profile.ProfileScreen
import com.example.sepotify.ui.search.SearchResultItem
import com.example.sepotify.ui.search.SearchScreen
import org.koin.compose.koinInject
import com.example.sepotify.ui.navigation.Routes.UserProfile
import com.example.sepotify.ui.profile.UserProfileScreen
import com.example.sepotify.ui.navigation.Routes.ArtistDetail
import com.example.sepotify.ui.navigation.Routes.PlaylistDetail
import com.example.sepotify.ui.playlist.PlaylistDetailScreen
import com.example.sepotify.ui.chat.ChatListScreen
import com.example.sepotify.ui.download.DownloadsScreen
import com.example.sepotify.ui.follow.FollowListScreen
import com.example.sepotify.ui.follow.FollowListViewModel
import com.example.sepotify.ui.playlist.PlaylistsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.sepotify.ui.home.LikedSongsScreen
import com.example.sepotify.ui.home.QuickAction
import com.example.sepotify.ui.home.RecentlyPlayedScreen
import com.example.sepotify.ui.settings.SettingsScreen

@Composable
fun MainScreen(
    userId: String,
    chatRepo: ChatRepository = koinInject(),
    followRepo: FollowRepository = koinInject(),
    onPlayerClick: () -> Unit,
    onLogout: () -> Unit
) {

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val playerManager: PlayerManager = koinInject()

    LaunchedEffect(userId) {
        playerManager.setUserId(userId)
        chatRepo.syncMessages(userId)
        chatRepo.startMessageRealtime(userId)
        followRepo.syncFollows(userId)
    }

    DisposableEffect(Unit) {
        onDispose {
            CoroutineScope(Dispatchers.IO).launch {
                chatRepo.stopMessageRealtime()
            }
        }
    }

    Scaffold(

        bottomBar = {

            Column {

                PlayerBar(
                    userId = userId,
                    onClick = onPlayerClick
                )

                BottomNavigationBar(
                    navController = navController,
                    currentDestination = backStackEntry?.destination
                )

            }

        }

    ) { padding ->

        NavHost(
            modifier = Modifier.padding(padding),
            navController = navController,
            startDestination = Routes.Home
        ) {

            composable<Routes.Home> {

                HomeScreen(
                    onSongClick = { queue, index ->
                        playerManager.playQueue(queue, index)
                    },
                    onSettingsClick = {
                        navController.navigate(Routes.Settings) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onProfileClick = {
                        navController.navigate(Routes.Profile) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onQuickActionClick = { action ->
                        when (action) {
                            QuickAction.LIKED -> navController.navigate(Routes.LikedSongs) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                    inclusive = false // don't remove Home itself
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            QuickAction.RECENT -> navController.navigate(Routes.RecentlyPlayed) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                    inclusive = false // don't remove Home itself
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            QuickAction.PLAYLISTS -> navController.navigate(Routes.Library) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                    inclusive = false // don't remove Home itself
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )

            }

            composable<Routes.Search> {
                SearchScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() },
                    onResultClick = { resultItem ->
                        when (resultItem) {
                            is SearchResultItem.Song -> {
                                // Play the song
                                playerManager.playSong(resultItem.song)
                            }
                            is SearchResultItem.Artist -> {
                                navController.navigate(Routes.ArtistDetail(resultItem.artist.id))
                            }
                            is SearchResultItem.Playlist -> {
                                navController.navigate(Routes.PlaylistDetail(resultItem.playlist.id))
                            }
                            is SearchResultItem.Profile -> {
                                navController.navigate(Routes.UserProfile(resultItem.profile.id))
                            }
                        }
                    }
                )
            }

            composable<UserProfile> { backStackEntry ->
                val args = backStackEntry.toRoute<UserProfile>()
                UserProfileScreen(
                    userId = args.userId,
                    currentUserId = userId,
                    onBack = { navController.popBackStack() },
                    onPlaylistClick = { playlistId ->
                        navController.navigate(Routes.PlaylistDetail(playlistId))
                    },
                    onSongClick = { songs, index -> playerManager.playQueue(songs, index) },
                    onChatClick = { otherUserId ->
                        navController.navigate(Routes.Chat(userId, otherUserId))
                    },
                    onFollowListClick = { userId, mode ->
                        navController.navigate(Routes.FollowList(userId = userId, currentUserId = userId, mode = mode))
                    }
                )
            }

            composable<ArtistDetail> { backStackEntry ->
                val args = backStackEntry.toRoute<ArtistDetail>()
                ArtistDetailScreen(
                    artistId = args.artistId,
                    onBack = { navController.popBackStack() },
                    onSongClick = { song -> playerManager.playSong(song) }
                )
            }

            composable<PlaylistDetail> { backStackEntry ->
                val args = backStackEntry.toRoute<PlaylistDetail>()
                PlaylistDetailScreen(
                    playlistId = args.playlistId,
                    onBack = { navController.popBackStack() },
                    onSongClick = { songs, index -> playerManager.playQueue(songs, index) }
                )
            }

            composable<Routes.Library> {
                PlaylistsScreen(
                    userId = userId,
                    onPlaylistClick = { playlistId ->
                        navController.navigate(Routes.PlaylistDetail(playlistId))
                    }
                )
            }

            composable<Routes.ChatList> {
                ChatListScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() },
                    onChatClick = { otherUserId ->
                        navController.navigate(Routes.Chat(userId, otherUserId))
                    }
                )
            }

            composable<Routes.Chat> { backStackEntry ->
                val args = backStackEntry.toRoute<Routes.Chat>()
                ChatScreen(
                    currentUserId = args.currentUserId,
                    otherUserId = args.otherUserId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Routes.Profile> {

                ProfileScreen(
                    onLogout = onLogout, //TODO: add stopMessageRealtime before it?
                    onDeleted = { },
                    onFollowListClick = { otherUserId, mode ->
                        navController.navigate(Routes.FollowList(userId = otherUserId, currentUserId = userId, mode = mode))
                    }
                )

            }

            composable<Routes.Settings> {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Routes.FollowList> { backStackEntry ->
                val args = backStackEntry.toRoute<Routes.FollowList>()
                val mode = if (args.mode == "following") FollowListViewModel.FollowListMode.FOLLOWING else FollowListViewModel.FollowListMode.FOLLOWERS
                FollowListScreen(
                    userId = args.userId,
                    currentUserId = args.currentUserId,
                    mode = mode,
                    onBack = { navController.popBackStack() },
                    onProfileClick = { profileId ->
                        navController.navigate(Routes.UserProfile(profileId))
                    }
                )
            }

            composable<Routes.LikedSongs> {
                LikedSongsScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() },
                    onSongClick = { songs, index -> playerManager.playQueue(songs, index) }
                )
            }

            composable<Routes.RecentlyPlayed> {
                RecentlyPlayedScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() },
                    onSongClick = { songs, index -> playerManager.playQueue(songs, index) }
                )
            }

            composable<Routes.Downloads> {
                DownloadsScreen(
                    userId = userId,
                    onSongClick = { songs, index ->
                        playerManager.playQueue(songs, index)
                    }
                )
            }

        }

    }

}