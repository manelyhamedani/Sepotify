// PlaylistDetailScreen.kt
package com.example.sepotify.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.sepotify.domain.model.Song
import com.example.sepotify.ui.components.SongItem
import com.example.sepotify.ui.theme.AppShape
import com.example.sepotify.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.example.sepotify.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    onBack: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    viewModel: PlaylistDetailViewModel = koinViewModel(
        parameters = { parametersOf(playlistId) }
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState) {
                is PlaylistDetailUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PlaylistDetailUiState.Success -> {
                    val playlist = (uiState as PlaylistDetailUiState.Success).playlist
                    Column {
                        // Playlist header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = playlist.coverUrl,
                                contentDescription = null,
                                placeholder = painterResource(R.drawable.ic_playlist_placeholder),
                                error = painterResource(R.drawable.ic_playlist_placeholder),
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = playlist.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                playlist.description?.let {
                                    Text(text = it, color = Color.Gray, fontSize = 14.sp)
                                }
                                Text(text = "${playlist.songs.size} songs", color = Color.Gray)
                            }
                        }

                        // Inside the Success state, after the header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.spaceSmall),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
                        ) {
                            Button(
                                onClick = { onSongClick(playlist.songs, 0) },
                                modifier = Modifier.weight(1f),
                                shape = AppShape.Button,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(Dimens.spaceSmall))
                                Text(stringResource(R.string.play_all))
                            }
                            OutlinedButton(
                                onClick = {
                                    val shuffled = playlist.songs.shuffled()
                                    onSongClick(shuffled, 0)
                                },
                                modifier = Modifier.weight(1f),
                                shape = AppShape.Button,
                                colors = ButtonDefaults.outlinedButtonColors()
                            ) {
                                Icon(Icons.Default.Shuffle, contentDescription = null)
                                Spacer(Modifier.width(Dimens.spaceSmall))
                                Text(stringResource(R.string.shuffle))
                            }
                        }

                        // Songs list
                        LazyColumn {
                            itemsIndexed(playlist.songs) { index, song ->
                                SongItem(
                                    song = song,
                                    onClick = { onSongClick(playlist.songs, index) }
                                )
                            }
                        }
                    }
                }
                is PlaylistDetailUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                        Text("Error: ${(uiState as PlaylistDetailUiState.Error).message}", color = Color.Red)
                    }
                }
            }
        }
    }
}