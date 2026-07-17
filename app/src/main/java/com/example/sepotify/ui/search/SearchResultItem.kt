package com.example.sepotify.ui.search

sealed class SearchResultItem {
    data class Song(val song: com.example.sepotify.domain.model.Song): SearchResultItem()
    data class Artist(val artist: com.example.sepotify.domain.model.Artist) : SearchResultItem()
    data class Playlist(val playlist: com.example.sepotify.domain.model.Playlist) : SearchResultItem()
    data class Profile(val profile: com.example.sepotify.domain.model.Profile) : SearchResultItem()
}