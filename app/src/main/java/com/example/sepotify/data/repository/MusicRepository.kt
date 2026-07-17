package com.example.sepotify.data.repository

import android.content.Context
import android.net.Uri
import androidx.paging.PagingData
import com.example.sepotify.domain.model.Artist
import com.example.sepotify.domain.model.Playlist
import com.example.sepotify.domain.model.Song
import com.example.sepotify.domain.model.DownloadState
import com.example.sepotify.utils.Result
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getTrendingSongs(): Result<List<Song>>
    suspend fun getNewReleases(): Result<List<Song>>
    suspend fun getLocalPlaylists(): Result<List<Playlist>>
    suspend fun getGlobalPlaylists(): Result<List<Playlist>>
    fun getAllPlaylists(userId: String): Flow<PagingData<Playlist>>

    fun getFavoriteSongs(userId: String): Flow<PagingData<Song>>
    suspend fun addToFavorites(userId: String, songId: String): Result<Unit>
    suspend fun removeFromFavorites(userId: String, songId: String): Result<Unit>

    fun getRecentlyPlayed(userId: String): Flow<PagingData<Song>>
    suspend fun addToRecentlyPlayed(userId: String, songId: String): Result<Unit>

    suspend fun createPlaylist(playlist: Playlist): Result<Playlist>
    suspend fun getPlaylist(id: String): Result<Playlist>
    suspend fun getUserPlaylists(userId: String): Result<List<Playlist>>
    fun getPublicUserPlaylists(userId: String): Flow<PagingData<Playlist>>
    suspend fun addSongToPlaylist(playlistId: String, songId: String): Result<Unit>
    suspend fun removeFromPlaylist(playlistId: String, songId: String): Result<Unit>

    suspend fun getArtist(id: String): Result<Artist>
    fun getArtists(): Flow<PagingData<Artist>>
    fun getSongsByArtists(artistId: String): Flow<PagingData<Song>>
    suspend fun isSongLiked(songId: String, userId: String): Result<Boolean>

    fun searchSongs(query: String): Flow<PagingData<Song>>
    fun searchArtists(query: String): Flow<PagingData<Artist>>
    fun searchPlaylists(query: String): Flow<PagingData<Playlist>>
    suspend fun searchSongsLimited(query: String, limit: Long = 4): Result<List<Song>>
    suspend fun searchArtistsLimited(query: String, limit: Long = 4): Result<List<Artist>>
    suspend fun searchPlaylistsLimited(query: String, limit: Long = 4): Result<List<Playlist>>
    suspend fun incrementPlayCount(songId: String): Result<Unit>

    suspend fun removeFromRecentlyPlayed(userId: String, songId: String): Result<Unit>
    suspend fun downloadSong(songId: String, userId: String): Result<String>
    suspend fun isSongDownloaded(songId: String): Boolean
    suspend fun getLocalFilePath(songId: String): String?
    suspend fun getLocalCoverPath(songId: String): String?
    fun getDownloadedSongs(): Flow<List<Song>>
    suspend fun deleteDownloadedSong(songId: String): Result<Unit>
    suspend fun getUserPremiumStatus(userId: String): Boolean
    suspend fun upgradeToPremium(userId: String): Result<Unit>
    fun getDownloadStatus(songId: String): Flow<DownloadState>
    suspend fun uploadPlaylistCover(playlistId: String, uri: Uri, context: Context): Result<String>
    suspend fun updatePlaylistCover(playlistId: String, coverUrl: String): Result<Unit>
}