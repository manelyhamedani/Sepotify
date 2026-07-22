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
import com.example.sepotify.domain.model.Song
import com.example.sepotify.ui.theme.AppShape
import com.example.sepotify.ui.theme.Dimens

@Composable
fun SongCard(
    song: Song,
    onClick: (Song) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(Dimens.cardSongWidth)
            .height(Dimens.cardSongHeight)
            .clickable { onClick(song) },
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
                model = song.coverUrl,
                placeholder = painterResource(R.drawable.ic_song_placeholder),
                error = painterResource(R.drawable.ic_song_placeholder),
                contentDescription = song.title,
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
                    text = song.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = song.artists.joinToString(", ") { it.name },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}