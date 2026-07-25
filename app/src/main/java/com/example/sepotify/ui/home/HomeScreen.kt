package com.example.sepotify.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.sepotify.domain.model.Playlist
import com.example.sepotify.domain.model.Song
import com.example.sepotify.ui.components.*
import com.example.sepotify.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onSongClick: (List<Song>, Int) -> Unit = { _, _ -> },
    onPlaylistClick: (Playlist) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onQuickActionClick: (QuickAction) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                onSettingsClick = onSettingsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            uiState.error?.let {
                ErrorView(
                    message = it,
                    onRetry = { viewModel.refresh() }
                )
                return@Box
            }

            val isLoading = uiState.isLoading
            val hasData = uiState.trendingSongs.isNotEmpty() ||
                    uiState.newReleases.isNotEmpty() ||
                    uiState.globalPlaylists.isNotEmpty() ||
                    uiState.localPlaylists.isNotEmpty()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceExtraLarge),
                contentPadding = PaddingValues(Dimens.paddingDoubleExtra)
            ) {
                // Show linear progress when refreshing with existing data
                if (isLoading && hasData) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                item {
                    QuickActionsRow(onActionClick = onQuickActionClick)
                }

                // Trending Songs
                item {
                    HorizontalSection(
                        title = "Trending",
                        items = if (isLoading && !hasData) {
                            List(5) { Song(audioUrl = "", durationSeconds = 0) } // placeholder
                        } else {
                            uiState.trendingSongs
                        },
                        emptyContent = { EmptyState(title = "No trending songs") },
                        isLoading = isLoading && !hasData
                    ) { index, song ->
                        if (song.audioUrl.isEmpty()) {
                            ShimmerSongCard()
                        } else {
                            SongCard(
                                song = song,
                                onClick = { onSongClick(uiState.trendingSongs, index) }
                            )
                        }
                    }
                }

                // New Releases
                item {
                    HorizontalSection(
                        title = "New Releases",
                        items = if (isLoading && !hasData) {
                            List(5) { Song(audioUrl = "", durationSeconds = 0) }
                        } else {
                            uiState.newReleases
                        },
                        emptyContent = { EmptyState(title = "No new releases") },
                        isLoading = isLoading && !hasData
                    ) { index, song ->
                        if (song.audioUrl.isEmpty()) {
                            ShimmerSongCard()
                        } else {
                            SongCard(
                                song = song,
                                onClick = { onSongClick(uiState.newReleases, index) }
                            )
                        }
                    }
                }

                // Global Playlists
                item {
                    HorizontalSection(
                        title = "Global Playlists",
                        items = if (isLoading && !hasData) {
                            List(3) { Playlist(id = "") }
                        } else {
                            uiState.globalPlaylists
                        },
                        emptyContent = { EmptyState(title = "No global playlists") },
                        isLoading = isLoading && !hasData
                    ) { index, playlist ->
                        if (playlist.id.isEmpty()) {
                            ShimmerPlaylistCard()
                        } else {
                            PlaylistCard(
                                playlist = playlist,
                                onClick = onPlaylistClick
                            )
                        }
                    }
                }

                // Local Playlists
                item {
                    HorizontalSection(
                        title = "Local Playlists",
                        items = if (isLoading && !hasData) {
                            List(3) { Playlist(id = "") }
                        } else {
                            uiState.localPlaylists
                        },
                        emptyContent = { EmptyState(title = "No local playlists") },
                        isLoading = isLoading && !hasData
                    ) { index, playlist ->
                        if (playlist.id.isEmpty()) {
                            ShimmerPlaylistCard()
                        } else {
                            PlaylistCard(
                                playlist = playlist,
                                onClick = onPlaylistClick
                            )
                        }
                    }
                }
            }
        }
    }
}