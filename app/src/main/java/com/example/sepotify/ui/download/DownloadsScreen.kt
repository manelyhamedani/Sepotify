package com.example.sepotify.ui.download

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.sepotify.R
import com.example.sepotify.domain.model.Song
import com.example.sepotify.ui.components.EmptyState
import com.example.sepotify.ui.components.SwipeToDismissSongItem
import com.example.sepotify.ui.theme.Dimens
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    userId: String,
    onSongClick: (List<Song>, Int) -> Unit,
    viewModel: DownloadsViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.downloads_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (downloadedSongs.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Download,
                    title = stringResource(R.string.no_downloads),
                    subtitle = stringResource(R.string.no_downloads_subtitle)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Dimens.spaceLarge)
                ) {
                    items(
                        items = downloadedSongs,
                        key = { it.id }
                    ) { song ->
                        SwipeToDismissSongItem(
                            song = song,
                            onRemove = {
                                scope.launch { viewModel.deleteDownload(song.id) }
                            },
                            onClick = {
                                onSongClick(downloadedSongs, downloadedSongs.indexOf(song))
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}