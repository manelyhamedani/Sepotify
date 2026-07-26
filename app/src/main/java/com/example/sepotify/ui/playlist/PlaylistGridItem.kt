package com.example.sepotify.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import coil3.compose.AsyncImage
import com.example.sepotify.R
import com.example.sepotify.domain.model.Playlist
import com.example.sepotify.domain.model.PlaylistType
import com.example.sepotify.ui.theme.AppShape
import com.example.sepotify.ui.theme.Dimens

@Composable
fun PlaylistGridItem(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = AppShape.Card,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevation)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Cover image with placeholder
            AsyncImage(
                model = playlist.coverUrl,
                placeholder = painterResource(R.drawable.ic_playlist_placeholder),
                error = painterResource(R.drawable.ic_playlist_placeholder),
                contentDescription = playlist.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Playlist name
            Text(
                text = playlist.name,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Dimens.paddingMedium),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Type badge (top-right) – optional, but adds clarity
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimens.paddingSmall),
                shape = RoundedCornerShape(Dimens.cardCornerRadius),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
            ) {
                Text(
                    text = when (playlist.type) {
                        PlaylistType.GLOBAL -> stringResource(R.string.playlist_type_global_short)
                        PlaylistType.LOCAL -> stringResource(R.string.playlist_type_local_short)
                        PlaylistType.USER -> stringResource(R.string.playlist_type_user_short)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(
                        horizontal = Dimens.paddingSmall,
                        vertical = Dimens.paddingExtraSmall
                    )
                )
            }
        }
    }
}