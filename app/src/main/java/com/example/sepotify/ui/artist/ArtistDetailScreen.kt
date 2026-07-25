package com.example.sepotify.ui.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.example.sepotify.R
import com.example.sepotify.domain.model.Song
import com.example.sepotify.ui.components.SongItem
import com.example.sepotify.ui.theme.AppShape
import com.example.sepotify.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistId: String,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    viewModel: ArtistDetailViewModel = koinViewModel(
        parameters = { parametersOf(artistId) }
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.artist_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
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
                is ArtistDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.iconExtraLarge)
                        )
                    }
                }

                is ArtistDetailUiState.Success -> {
                    val artist = (uiState as ArtistDetailUiState.Success).artist
                    val songsPagingItems = (uiState as ArtistDetailUiState.Success).songs.collectAsLazyPagingItems()
                    val allSongs = songsPagingItems.itemSnapshotList.items

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Artist header with gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.artistHeaderHeight)
                                .background(MaterialTheme.colorScheme.surfaceVariant) // we'll add this
                        ) {
                            // Artist cover image
                            AsyncImage(
                                model = artist.avatarUrl,
                                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                                error = painterResource(R.drawable.ic_profile_placeholder),
                                contentDescription = artist.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                            // Gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.7f)
                                            ),
                                            startY = 0.4f,
                                            endY = 1f
                                        )
                                    )
                            )

                            // Artist info at bottom of header
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(Dimens.paddingDoubleExtra)
                            ) {
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!artist.bio.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                                    Text(
                                        text = artist.bio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(Dimens.spaceMedium))

                                // Action buttons row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
                                ) {
                                    // Play All
                                    Button(
                                        onClick = {
                                            if (allSongs.isNotEmpty()) {
                                                allSongs.forEach { onSongClick(it) }
                                            }
                                        },
                                        shape = AppShape.Button,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(Dimens.iconSmall)
                                        )
                                        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                                        Text(stringResource(R.string.play_all))
                                    }

                                    // Shuffle
                                    OutlinedButton(
                                        onClick = {
                                            if (allSongs.isNotEmpty()) {
                                                allSongs.shuffled().forEach { onSongClick(it) }
                                            }
                                        },
                                        shape = AppShape.Button,
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier
                                    ) {
                                        Icon(
                                            Icons.Default.Shuffle,
                                            contentDescription = null,
                                            modifier = Modifier.size(Dimens.iconSmall)
                                        )
                                        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                                        Text(stringResource(R.string.shuffle))
                                    }
                                }
                            }
                        }

                        // Songs list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Dimens.spaceMedium),
                            contentPadding = PaddingValues(
                                top = Dimens.spaceMedium,
                                bottom = Dimens.spaceLarge
                            ),
                            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                        ) {
                            // Songs
                            for (index in 0 until songsPagingItems.itemCount) {
                                val song = songsPagingItems[index]
                                if (song != null) {
                                    item {
                                        SongItem(
                                            song = song,
                                            onClick = { onSongClick(song) }
                                        )
                                    }
                                }
                            }

                            // Loading states
                            when {
                                songsPagingItems.loadState.refresh is LoadState.Loading -> {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(Dimens.iconMedium),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                songsPagingItems.loadState.append is LoadState.Loading -> {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(Dimens.iconMedium),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                songsPagingItems.loadState.refresh is LoadState.Error -> {
                                    item {
                                        Text(
                                            text = stringResource(R.string.load_error),
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(Dimens.spaceMedium)
                                        )
                                    }
                                }
                                songsPagingItems.itemCount == 0 && songsPagingItems.loadState.refresh !is LoadState.Loading -> {
                                    item {
                                        Text(
                                            text = stringResource(R.string.no_songs),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(Dimens.spaceMedium)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is ArtistDetailUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${stringResource(R.string.error)}: ${(uiState as ArtistDetailUiState.Error).message}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                            Button(
                                onClick = { /* retry logic */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
            }
        }
    }
}