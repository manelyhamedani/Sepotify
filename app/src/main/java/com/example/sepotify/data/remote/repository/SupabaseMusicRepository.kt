package com.example.sepotify.data.remote.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.sepotify.data.paging.SupabasePagingSource
import com.example.sepotify.data.remote.Supabase
import com.example.sepotify.data.remote.utils.runResult
import com.example.sepotify.data.repository.MusicRepository
import com.example.sepotify.domain.model.Artist
import com.example.sepotify.domain.model.Playlist
import com.example.sepotify.domain.model.PlaylistSongInsert
import com.example.sepotify.domain.model.Song
import com.example.sepotify.utils.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject
import com.example.sepotify.data.local.database.dao.DownloadedSongDao
import android.content.Context
import android.net.Uri
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.sepotify.data.repository.ProfileRepository
import com.example.sepotify.data.worker.DownloadWorker
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.File
import com.example.sepotify.domain.model.DownloadState
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit


class SupabaseMusicRepository(
    private val client: SupabaseClient = Supabase.client,
    private val downloadedSongDao: DownloadedSongDao,
    private val context: Context,
    private val json: Json = Supabase.json,
    private val profileRepo: ProfileRepository
): MusicRepository {

    override suspend fun getTrendingSongs(): Result<List<Song>> =
        runResult("Failed to fetch trending songs") {
            client.postgrest["song_details"]
                .select {
                    order("play_count", Order.DESCENDING)
                    limit(20)
                }
                .decodeList<Song>()
        }


    override suspend fun getNewReleases(): Result<List<Song>> =
        runResult("Failed to fetch new releases") {
            client.postgrest["song_details"]
                .select {
                    order("created_at", Order.DESCENDING)
                    limit(20)
                }
                .decodeList<Song>()
        }

    override suspend fun getLocalPlaylists(): Result<List<Playlist>> =
        runResult("Failed to fetch local playlists") {
            client.postgrest["playlists"]
                .select {
                    filter {
                        eq("type", "LOCAL")
                        eq("is_public", true)
                    }

                    order("created_at", Order.DESCENDING)
                    limit(20)
                }
                .decodeList()
        }

    override suspend fun getGlobalPlaylists(): Result<List<Playlist>> =
        runResult("Failed to fetch global playlists") {
            client.postgrest["playlists"]
                .select {
                    filter {
                        eq("type", "GLOBAL")
                        eq("is_public", true)
                    }

                    order("created_at", Order.DESCENDING)
                    limit(20)
                }
                .decodeList()
        }

    override fun getFavoriteSongs(userId: String): Flow<PagingData<Song>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SupabasePagingSource { from, to ->
                    client.postgrest["favorite_song_details"]
                        .select {
                            filter {
                                eq("user_id", userId)
                            }
                            order("added_at", Order.DESCENDING)
                            range(from, to)
                        }
                        .decodeList<Song>()
                }
            }
        ).flow

    override suspend fun addToFavorites(
        userId: String,
        songId: String
    ): Result<Unit> = runResult("Failed to like song") {
        Log.d("Supabase", "going to like song=$songId")
        client.postgrest["liked_songs"].insert(
            mapOf(
                "user_id" to userId,
                "song_id" to songId
            )
        )
        Log.d("Supabase", "liked song=$songId")
    }

    override suspend fun removeFromFavorites(
        userId: String,
        songId: String
    ): Result<Unit> = runResult("Failed to unlike song") {
        client.postgrest["liked_songs"].delete {
            filter {
                eq("user_id", userId)
                eq("song_id", songId)
            }
        }
    }
    override fun getRecentlyPlayed(userId: String): Flow<PagingData<Song>>  =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SupabasePagingSource { from, to ->
                    client.postgrest["recently_played_song_details"]
                        .select {
                            filter {
                                eq("user_id", userId)
                            }
                            order("played_at", Order.DESCENDING)
                            range(from, to)
                        }
                        .decodeList<Song>()
                }
            }
        ).flow

    override suspend fun addToRecentlyPlayed(
        userId: String,
        songId: String
    ): Result<Unit> = runResult("Failed to add to recently played") {
        client.postgrest["recently_played"].insert(
            mapOf(
                "user_id" to userId,
                "song_id" to songId
            )
        )
    }

    override suspend fun createPlaylist(playlist: Playlist): Result<Playlist> =
        runResult("Failed to create playlist") {
            Log.d("Supabase", "Going to create playlist")
            val created = client.postgrest["playlists"]
                .insert(
                   playlist
                )
                {
                    select()
                }
                .decodeSingle<Playlist>()
            Log.d("Supabase", "Playlist created")

            if (playlist.songs.isNotEmpty()) {
                client.postgrest["playlist_songs"]
                    .insert(
                        playlist.songs.mapIndexed { index, song ->
                            mapOf(
                                "playlist_id" to created.id,
                                "song_id" to song.id,
                                "position" to index
                            )
                        }
                    )
            }

            created.copy(songs = playlist.songs)
    }

    override suspend fun getPlaylist(id: String): Result<Playlist> =
        runResult("Failed to fetch playlist") {
            val playlist = client.postgrest["playlists"]
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<Playlist>()

            val songs = client.postgrest["playlist_song_details"]
                .select {
                    filter {
                        eq("playlist_id", id)
                    }

                    order("position", Order.ASCENDING)
                }
                .decodeList<Song>()

            playlist.copy(songs = songs)
        }

    override suspend fun getUserPlaylists(userId: String): Result<List<Playlist>>  =
        runResult("Failed to load user playlists") {
            client.postgrest["playlists"]
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Playlist>()
        }

    override fun getPublicUserPlaylists(userId: String): Flow<PagingData<Playlist>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SupabasePagingSource { from, to ->
                    client.postgrest["playlists"]
                        .select {
                            filter {
                                eq("user_id", userId)
                                eq("is_public", true)
                            }

                            order("created_at", Order.DESCENDING)

                            range(from, to)
                        }
                        .decodeList<Playlist>()
                }
            }
        ).flow

    override suspend fun addSongToPlaylist(
        playlistId: String,
        songId: String
    ): Result<Unit> =
        runResult("Failed to add song") {
            val currentCount = client.postgrest["playlist_songs"]
                .select {
                    filter {
                        eq("playlist_id", playlistId)
                    }
                }
                .decodeList<JsonObject>()
                .size

            Log.d("Supabase", "found position of song = $currentCount")

            client.postgrest["playlist_songs"]
                .insert(
                    PlaylistSongInsert(
                        playlistId = playlistId,
                        songId = songId,
                        position = currentCount + 1
                    )
                )
        }

    override suspend fun removeFromPlaylist(
        playlistId: String,
        songId: String
    ): Result<Unit> =
        runResult("Failed to remove song") {

            client.postgrest["playlist_songs"]
                .delete {
                    filter {
                        eq("playlist_id", playlistId)
                        eq("song_id", songId)
                    }
                }
        }

    override suspend fun getArtist(id: String): Result<Artist> =
        runResult("Failed to fetch artist") {
            client.postgrest["artists"]
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<Artist>()
        }

    override fun getArtists(): Flow<PagingData<Artist>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SupabasePagingSource { from, to ->
                    client.postgrest["artists"]
                        .select {
                            order("name", Order.DESCENDING)
                            range(from, to)
                        }
                        .decodeList<Artist>()
                }
            }
        ).flow

    override fun getSongsByArtists(artistId: String): Flow<PagingData<Song>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SupabasePagingSource { from, to ->
                    val songIds = client.postgrest["song_artists"]
                        .select {
                            filter {
                                eq("artist_id", artistId)
                            }
                            range(from, to)
                        }
                        .decodeList<Map<String, String>>()
                        .map { it["song_id"]!! }

                    if (songIds.isEmpty()) {
                        emptyList()
                    }
                    else {
                        client.postgrest["song_details"]
                            .select {
                                filter {
                                    isIn("id", songIds)
                                }
                            }
                            .decodeList<Song>()
                    }
                }
            }
        ).flow

    override fun searchSongs(query: String): Flow<PagingData<Song>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SupabasePagingSource { from, to ->
                    client.postgrest["song_details"]
                        .select {
                            filter {
                                ilike("title", "%$query%")
                            }
                            range(from, to)
                        }
                        .decodeList<Song>()
                }
            }
        ).flow

    override fun searchArtists(query: String): Flow<PagingData<Artist>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SupabasePagingSource { from, to ->
                    client.postgrest["artists"]
                        .select {
                            filter {
                                ilike("name", "%$query%")
                            }
                            range(from, to)
                        }
                        .decodeList<Artist>()
                }
            }
        ).flow

    override fun searchPlaylists(query: String): Flow<PagingData<Playlist>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SupabasePagingSource { from, to ->
                    client.postgrest["playlists"]
                        .select {
                            filter {
                                or {
                                    ilike("name", "%$query%")
                                    ilike("description", "%$query%")
                                }
                            }
                            range(from, to)
                        }
                        .decodeList<Playlist>()
                }
            }
        ).flow

    override suspend fun searchSongsLimited(query: String, limit: Long): Result<List<Song>> =
        runResult("Failed to search songs") {
            client.postgrest["song_details"]
                .select {
                    filter {
                        ilike("title", "%$query%")
                    }
                    limit(limit)
                }
                .decodeList<Song>()
        }

    override suspend fun searchArtistsLimited(query: String, limit: Long): Result<List<Artist>> =
        runResult("Failed to search artists") {
            client.postgrest["artists"]
                .select {
                    filter {
                        ilike("name", "%$query%")
                    }
                    limit(limit)
                }
                .decodeList<Artist>()
        }

    override suspend fun searchPlaylistsLimited(query: String, limit: Long): Result<List<Playlist>> =
        runResult("Failed to search playlists") {
            client.postgrest["playlists"]
                .select {
                    filter {
                        or {
                            ilike("name", "%$query%")
                            ilike("description", "%$query%")
                        }
                    }
                    limit(limit)
                }
                .decodeList<Playlist>()
        }

    override fun getAllPlaylists(userId: String): Flow<PagingData<Playlist>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SupabasePagingSource { from, to ->
                    client.postgrest["playlists"]
                        .select {
                            filter {
                                or {
                                    eq("type", "GLOBAL")
                                    eq("type", "LOCAL")
                                    and {
                                        eq("type", "USER")
                                        eq("user_id", userId)
                                    }
                                }
                            }
                            order("type", Order.ASCENDING)
                            order("created_at", Order.DESCENDING)
                            range(from, to)
                        }
                        .decodeList<Playlist>()
                }
            }
        ).flow

    override suspend fun isSongLiked(songId: String, userId: String): Result<Boolean> =
        runResult("Failed to fetch like status of song") {
            client.postgrest["liked_songs"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("song_id", songId)
                    }
                }
                .decodeList<JsonObject>().isNotEmpty()
        }

    override suspend fun incrementPlayCount(songId: String): Result<Unit> =
        runResult("Failed to increment play count") {
            //TODO: need lock? or Supabase handle that?
            val current = client.postgrest["songs"]
                .select(columns = Columns.raw("play_count")) {
                    filter { eq("id", songId) }
                }
                .decodeSingle<Map<String, Long>>()
                .values
                .firstOrNull() ?: 0L

            client.postgrest["songs"]
                .update(
                    mapOf("play_count" to (current + 1))
                ) {
                    filter { eq("id", songId) }
                }
        }

    override suspend fun removeFromRecentlyPlayed(userId: String, songId: String): Result<Unit> =
        runResult("Failed to remove from recently played") {
            client.postgrest["recently_played"]
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("song_id", songId)
                    }
                }
        }

    override suspend fun downloadSong(
        songId: String,
        userId: String
    ): Result<String> {
        val isPremium = getUserPremiumStatus(userId)
        if (!isPremium) return Result.Error("Premium required to download")
        Log.d("Supabase", "going to fetch song details to download")
        val song = client.postgrest["song_details"]
            .select { filter { eq("id", songId) } }
            .decodeSingle<Song>()

        val songJson = json.encodeToString(song)
        Log.d("Supabase", "going to download song $songId")
        WorkManager.getInstance(context).cancelAllWorkByTag(songId)

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .setInputData(
                workDataOf(
                    "songId" to songId,
                    "songJson" to songJson
                )
            )
            .addTag(songId)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        Log.d("Supabase", "Download work enqueued for song $songId")

        return Result.Success("Success")
    }

    override suspend fun isSongDownloaded(songId: String): Boolean =
        downloadedSongDao.getSong(songId) != null


    override suspend fun getLocalFilePath(songId: String): String? {
        val entity = downloadedSongDao.getSong(songId)
        val path = entity?.filePath
        if (path != null && File(path).exists()) {
            Log.d("Supabase", "File path exists")
            return path
        }
        else {
            Log.d("Supabase", "File path doesn't exist")
            return null
        }
    }

    override suspend fun getLocalCoverPath(songId: String): String? = downloadedSongDao.getCoverPath(songId)

    override fun getDownloadedSongs(): Flow<List<Song>> =
        downloadedSongDao.getAll().map { entities ->
            entities.mapNotNull { entity ->
                try { json.decodeFromString(entity.songData) } catch (_: Exception) { null }
            }
        }

    override suspend fun deleteDownloadedSong(songId: String): Result<Unit>  =
        runResult("Failed to delete") {
            downloadedSongDao.delete(songId)
            // Also delete the file
            val file = getLocalFilePath(songId)?.let { File(it) }
            file?.delete()

            WorkManager.getInstance(context).cancelAllWorkByTag(songId)
        }


    override suspend fun getUserPremiumStatus(userId: String): Boolean {
        return when (val profile = profileRepo.getProfile(userId)) {
            is Result.Success -> profile.data.isPremium
            else -> false
        }
    }

    override suspend fun upgradeToPremium(userId: String): Result<Unit> =
        profileRepo.upgradeToPremium(userId)

    override fun getDownloadStatus(songId: String): Flow<DownloadState> {
        return WorkManager.getInstance(context)
            .getWorkInfosByTagFlow(songId)
            .map { infos ->
                val info = infos.firstOrNull()
                when (info?.state) {
                    null -> DownloadState.NotStarted
                    WorkInfo.State.ENQUEUED -> DownloadState.Pending
                    WorkInfo.State.RUNNING -> {
                        val progress = info.progress.getFloat("progress", 0f)
                        DownloadState.Downloading(progress)
                    }
                    WorkInfo.State.SUCCEEDED -> DownloadState.Completed
                    WorkInfo.State.FAILED -> {
                        // If the song is already downloaded, consider it completed,
                        // otherwise treat as not started (clear the stale error).
                        if (isSongDownloaded(songId)) {
                            DownloadState.Completed
                        } else {
                            DownloadState.NotStarted
                        }
                    }
                    else -> DownloadState.NotStarted
                }
            }
    }


    override suspend fun uploadPlaylistCover(playlistId: String, uri: Uri, context: Context): Result<String> {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: return Result.Error("Cannot read image")
            val fileName = "playlists/$playlistId/cover.jpg"
            val bucket = client.storage["assets"]
            bucket.upload(
                path = fileName,
                data = bytes,
                options = { upsert = true }
            )
            val publicUrl = bucket.publicUrl(fileName)
            Result.Success(publicUrl)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Upload failed")
        }
    }

    override suspend fun updatePlaylistCover(playlistId: String, coverUrl: String): Result<Unit> {
        return try {
            client.postgrest["playlists"]
                .update(mapOf("cover_url" to coverUrl)) {
                    filter { eq("id", playlistId) }
                }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update cover")
        }
    }
}