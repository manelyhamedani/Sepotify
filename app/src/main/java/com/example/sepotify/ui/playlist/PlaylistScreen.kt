package com.example.sepotify.ui.playlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.sepotify.R
import com.example.sepotify.domain.model.PlaylistType
import com.example.sepotify.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    userId: String,
    onPlaylistClick: (String) -> Unit,
    viewModel: PlaylistsViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    when (uiState) {
        is PlaylistsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is PlaylistsUiState.Success -> {
            val pagingItems = (uiState as PlaylistsUiState.Success).pagingFlow.collectAsLazyPagingItems()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.playlists_title)) },
                        actions = {
                            IconButton(onClick = { showCreateDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_playlist))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    if (pagingItems.itemCount == 0 && pagingItems.loadState.refresh !is LoadState.Loading) {
                        EmptyPlaylistState(
                            onAction = { showCreateDialog = true }
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Dimens.spaceMedium),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium),
                            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
                        ) {
                            var previousType: PlaylistType? = null

                            for (index in 0 until pagingItems.itemCount) {
                                val playlist = pagingItems[index]
                                if (playlist != null) {
                                    if (playlist.type != previousType) {
                                        item(span = { GridItemSpan(maxLineSpan) }) {
                                            SectionHeader(type = playlist.type)
                                        }
                                        previousType = playlist.type
                                    }
                                    item {
                                        PlaylistGridItem(
                                            playlist = playlist,
                                            onClick = { onPlaylistClick(playlist.id) }
                                        )
                                    }
                                }
                            }

                            // Loading/Error states
                            when {
                                pagingItems.loadState.refresh is LoadState.Loading -> {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(Dimens.iconMedium))
                                        }
                                    }
                                }
                                pagingItems.loadState.refresh is LoadState.Error -> {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Text(
                                            text = stringResource(R.string.load_error),
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(Dimens.spaceMedium)
                                        )
                                    }
                                }
                            }
                            // Append loading
                            if (pagingItems.loadState.append is LoadState.Loading) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(Dimens.iconSmall))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showCreateDialog) {
                CreatePlaylistDialog(
                    onDismiss = { showCreateDialog = false },
                    onCreate = { name, description, isPublic, coverUri ->
                        viewModel.createPlaylist(name, description, isPublic, coverUri)
                        showCreateDialog = false
                    }
                )
            }
        }

        is PlaylistsUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (uiState as PlaylistsUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                    Button(onClick = { viewModel.loadPlaylists(userId) }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(type: PlaylistType) {
    val label = when (type) {
        PlaylistType.GLOBAL -> stringResource(R.string.playlist_type_global)
        PlaylistType.LOCAL -> stringResource(R.string.playlist_type_local)
        PlaylistType.USER -> stringResource(R.string.playlist_type_user)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spaceSmall)
    )
}

@Composable
private fun EmptyPlaylistState(onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_playlists_yet),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(Dimens.spaceMedium))
        Button(onClick = onAction) {
            Text(stringResource(R.string.create_new_playlist))
        }
    }
}