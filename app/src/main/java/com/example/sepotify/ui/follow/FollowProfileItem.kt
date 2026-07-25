package com.example.sepotify.ui.follow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.example.sepotify.R
import com.example.sepotify.domain.model.Profile
import com.example.sepotify.ui.theme.AppShape
import com.example.sepotify.ui.theme.Dimens

@Composable
fun FollowProfileItem(
    profile: Profile,
    showUnfollow: Boolean = false,
    onUnfollow: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = AppShape.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.cardElevation
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.paddingLarge,
                    vertical = Dimens.paddingMedium
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar (with placeholder)
            AsyncImage(
                model = profile.avatarUrl,
                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                error = painterResource(R.drawable.ic_profile_placeholder),
                contentDescription = null,
                modifier = Modifier
                    .size(Dimens.avatarMedium)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(Dimens.spaceMedium))

            // Name and username
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = profile.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${profile.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}