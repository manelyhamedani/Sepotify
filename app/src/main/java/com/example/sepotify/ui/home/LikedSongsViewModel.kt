package com.example.sepotify.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.sepotify.data.paging.SupabasePagingSource
import com.example.sepotify.data.remote.Supabase.client
import com.example.sepotify.data.repository.MusicRepository
import com.example.sepotify.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import com.example.sepotify.utils.Result
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class LikedSongsViewModel(
    private val musicRepo: MusicRepository,
    private val userId: String
) : ViewModel() {

    private var pagingSource: SupabasePagingSource<Song>? = null
    private val _removedIds = MutableStateFlow<Set<String>>(emptySet())

    val songs: Flow<PagingData<Song>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false)
    ) {
        SupabasePagingSource { from, to ->
            client.postgrest["favorite_song_details"]
                .select {
                    filter { eq("user_id", userId) }
                    order("added_at", Order.DESCENDING)
                    range(from, to)
                }
                .decodeList<Song>()                     // extra fields ignored
                .distinctBy { it.id }
                .filter { it.id !in _removedIds.value }
        }.also { pagingSource = it }
    }.flow.cachedIn(viewModelScope)

    fun unlikeSong(songId: String) {
        _removedIds.value += songId
        pagingSource?.invalidate()
        viewModelScope.launch {
            val result = musicRepo.removeFromFavorites(userId, songId)
            if (result is Result.Error) {
                _removedIds.value -= songId
                pagingSource?.invalidate()
            }
        }
    }
}