package com.example.sepotify.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.sepotify.R
import com.example.sepotify.domain.model.Song
import com.example.sepotify.ui.components.EmptyState
import com.example.sepotify.ui.components.PlaylistHeader
import com.example.sepotify.ui.components.SwipeToDismissSongItem
import com.example.sepotify.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyPlayedScreen(
    userId: String,
    onBack: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    viewModel: RecentlyPlayedViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val pagingItems = viewModel.songs.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recently_played_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = Dimens.spaceLarge)
            ) {
                // Header
                item {
                    PlaylistHeader(
                        title = stringResource(R.string.recently_played_title),
                        coverUrl = null,
                        songCount = pagingItems.itemCount,
                        onPlayAll = {
                            val songs = pagingItems.itemSnapshotList.items
                            if (songs.isNotEmpty()) onSongClick(songs, 0)
                        },
                        onShuffle = {
                            val songs = pagingItems.itemSnapshotList.items
                            if (songs.isNotEmpty()) onSongClick(songs.shuffled(), 0)
                        },
                        showShuffle = true
                    )
                    Divider()
                }

                // Songs
                itemsIndexed(
                    items = pagingItems.itemSnapshotList.items,
                    key = { _, song -> song.id }
                ) { index, song ->
                    SwipeToDismissSongItem(
                        song = song,
                        onRemove = { viewModel.removeFromHistory(song.id) },
                        onClick = {
                            val allSongs = pagingItems.itemSnapshotList.items
                            onSongClick(allSongs, index)
                        }
                    )
                    Divider()
                }

                // Loading/Error
                when {
                    pagingItems.loadState.refresh is LoadState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(Dimens.iconMedium))
                            }
                        }
                    }
                    pagingItems.loadState.refresh is LoadState.Error -> {
                        item {
                            Text(
                                text = stringResource(R.string.load_error),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(Dimens.spaceMedium)
                            )
                        }
                    }
                }
            }

            // Empty state
            if (pagingItems.itemCount == 0 && pagingItems.loadState.refresh !is LoadState.Loading) {
                EmptyState(
                    icon = Icons.Outlined.History,
                    title = stringResource(R.string.no_recently_played),
                    subtitle = stringResource(R.string.no_recently_played_subtitle)
                )
            }
        }
    }
}