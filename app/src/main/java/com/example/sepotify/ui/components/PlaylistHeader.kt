package com.example.sepotify.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.example.sepotify.R
import com.example.sepotify.ui.theme.AppShape
import com.example.sepotify.ui.theme.Dimens

@Composable
fun PlaylistHeader(
    title: String,
    coverUrl: String?,
    songCount: Int,
    onPlayAll: () -> Unit,
    onShuffle: (() -> Unit)? = null,
    showShuffle: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.paddingLarge),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cover image with placeholder
        AsyncImage(
            model = coverUrl,
            placeholder = painterResource(R.drawable.ic_playlist_placeholder),
            error = painterResource(R.drawable.ic_playlist_placeholder),
            contentDescription = title,
            modifier = Modifier
                .size(Dimens.avatarExtraLarge)
                .clip(RoundedCornerShape(Dimens.cardCornerRadius)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(Dimens.spaceMedium))

        // Title and metadata
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.song_count, songCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            // Play All
            IconButton(
                onClick = onPlayAll,
                modifier = Modifier.size(Dimens.iconLarge)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.play_all),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Shuffle
            if (showShuffle && onShuffle != null) {
                IconButton(
                    onClick = onShuffle,
                    modifier = Modifier.size(Dimens.iconLarge)
                ) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = stringResource(R.string.shuffle),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}