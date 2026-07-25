package com.example.sepotify.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.example.sepotify.R
import com.example.sepotify.ui.theme.AppShape
import com.example.sepotify.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerBar(
    userId: String,
    onClick: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val state by viewModel.playerState.collectAsState()

    AnimatedVisibility(
        visible = state.currentSong != null
    ) {
        val song = state.currentSong ?: return@AnimatedVisibility

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spaceLarge)
                .clip(RoundedCornerShape(Dimens.cardCornerRadius))
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = Dimens.cardElevation * 2
            )
        ) {
            Column {
                // Progress bar
                LinearProgressIndicator(
                    progress = {
                        if (state.duration <= 0) 0f
                        else state.currentPosition.toFloat() / state.duration.toFloat()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.playerBarProgressHeight),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.playerBarHeight)
                        .padding(horizontal = Dimens.spaceMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album art
                    AsyncImage(
                        model = song.coverUrl,
                        placeholder = painterResource(R.drawable.ic_song_placeholder),
                        error = painterResource(R.drawable.ic_song_placeholder),
                        contentDescription = song.title,
                        modifier = Modifier
                            .size(Dimens.iconExtraLarge)
                            .clip(RoundedCornerShape(Dimens.cardCornerRadius))
                    )

                    Spacer(modifier = Modifier.width(Dimens.spaceMedium))

                    // Title & artist
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artists.joinToString(", ") { it.name },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Play / Pause
                    IconButton(
                        onClick = { viewModel.playPause() },
                        modifier = Modifier.size(Dimens.iconLarge)
                    ) {
                        Crossfade(state.isPlaying) { playing ->
                            Icon(
                                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playing) stringResource(R.string.pause) else stringResource(R.string.play),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Skip Next
                    IconButton(
                        onClick = { viewModel.next() },
                        modifier = Modifier.size(Dimens.iconLarge)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = stringResource(R.string.next),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Stop button
                    IconButton(
                        onClick = { viewModel.stopPlayback() },
                        modifier = Modifier.size(Dimens.iconLarge)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.stop),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}