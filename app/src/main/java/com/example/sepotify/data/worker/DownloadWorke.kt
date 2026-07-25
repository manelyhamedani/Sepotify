package com.example.sepotify.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.sepotify.data.local.database.dao.DownloadedSongDao
import com.example.sepotify.data.local.database.entity.DownloadedSongEntity
import com.example.sepotify.data.remote.Supabase
import com.example.sepotify.domain.model.Song
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import java.util.concurrent.TimeUnit

class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val dao: DownloadedSongDao by inject()

    private val downloadClient by lazy {
        HttpClient(OkHttp) {
            engine {
                config {
                    connectTimeout(60, TimeUnit.SECONDS)
                    readTimeout(60, TimeUnit.SECONDS)
                    writeTimeout(60, TimeUnit.SECONDS)
                }
            }
        }
    }

    override suspend fun doWork(): Result {
        val songId = inputData.getString("songId") ?: return Result.failure()
        val songJson = inputData.getString("songJson") ?: return Result.failure()

        return withContext(Dispatchers.IO) {
            try {
                val song = Supabase.json.decodeFromString<Song>(songJson)

                // Download audio
                val audioFile = downloadFile(song.audioUrl, "$songId.mp3")
                if (audioFile == null) return@withContext Result.failure()

                // Download cover (optional)
                var coverFile: File? = null
                if (!song.coverUrl.isNullOrEmpty()) {
                    coverFile = downloadFile(song.coverUrl, "${songId}_cover.jpg")
                }

                // Save to Room
                dao.insert(
                    DownloadedSongEntity(
                        songId = songId,
                        songData = songJson,
                        filePath = audioFile.absolutePath,
                        coverPath = coverFile?.absolutePath,
                        downloadedAt = System.currentTimeMillis()
                    )
                )

                // Notify progress (optional)
                setProgress(workDataOf("progress" to 1f))

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    private suspend fun downloadFile(url: String, fileName: String): File? {
        return try {
            val response = downloadClient.get(url)
            val bytes = response.bodyAsBytes()
            val dir = File(applicationContext.filesDir, "downloads")
            dir.mkdirs()
            val file = File(dir, fileName)
            file.writeBytes(bytes)
            file
        } catch (e: Exception) {
            null
        }
    }
}