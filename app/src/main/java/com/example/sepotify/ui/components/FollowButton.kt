// ui/components/FollowButton.kt
package com.example.sepotify.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FollowButton(
    isFollowing: Boolean,
    isLoading: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { if (isFollowing) onUnfollow() else onFollow() },
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowing) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
            contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        } else {
            Text(if (isFollowing) "Unfollow" else "Follow")
        }
    }
}