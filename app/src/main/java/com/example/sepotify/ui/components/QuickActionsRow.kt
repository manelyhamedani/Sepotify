package com.example.sepotify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.sepotify.ui.home.QuickAction
import com.example.sepotify.ui.theme.Dimens

@Composable
fun QuickActionsRow(
    onActionClick: (QuickAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spaceLarge, vertical = Dimens.spaceSmall),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickAction.entries.forEach { action ->
            Box(
                modifier = Modifier
                    .size(Dimens.buttonHeight)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { onActionClick(action) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    modifier = Modifier.size(Dimens.iconLarge),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}