package com.example.sepotify.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class QuickAction(val title: String, val icon: ImageVector) {
    LIKED("Liked Songs", Icons.Outlined.Favorite),
    RECENT("Recently Played", Icons.Outlined.History),
    PLAYLISTS("My Playlists", Icons.Outlined.LibraryMusic),
}