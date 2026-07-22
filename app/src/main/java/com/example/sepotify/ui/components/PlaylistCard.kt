package com.example.sepotify.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.example.sepotify.R
import com.example.sepotify.domain.model.Playlist
import com.example.sepotify.ui.theme.AppShape
import com.example.sepotify.ui.theme.Dimens

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: (Playlist) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(Dimens.cardSongWidth)
            .height(Dimens.cardSongHeight)
            .clickable { onClick(playlist) },
        shape = AppShape.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.cardElevation
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            AsyncImage(
                model = playlist.coverUrl,
                placeholder = painterResource(R.drawable.ic_playlist_placeholder),
                error = painterResource(R.drawable.ic_playlist_placeholder),
                contentDescription = playlist.name,
                modifier = Modifier
                    .size(Dimens.cardImageSize)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingSmall)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = playlist.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}