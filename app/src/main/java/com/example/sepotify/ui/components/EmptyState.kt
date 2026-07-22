package com.example.sepotify.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.sepotify.ui.theme.Dimens
import com.example.sepotify.ui.theme.unselectedIcon

@Composable
fun EmptyState(
    icon: ImageVector = Icons.Outlined.LibraryMusic,
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingDoubleExtra),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = unselectedIcon),
            modifier = Modifier.size(Dimens.iconExtraLarge)
        )
        Spacer(modifier = Modifier.height(Dimens.spaceMedium))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}