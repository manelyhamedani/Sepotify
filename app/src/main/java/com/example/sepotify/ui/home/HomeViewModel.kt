package com.example.sepotify.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.sepotify.data.repository.MusicRepository
import com.example.sepotify.utils.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()


    init {
        loadHome()
    }

    fun refresh() = loadHome()

    private fun loadHome() = viewModelScope.launch {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        val trending = async { repository.getTrendingSongs() }
        val releases = async { repository.getNewReleases() }
        val global = async { repository.getGlobalPlaylists() }
        val local = async { repository.getLocalPlaylists() }

        when (val trendingResult = trending.await()) {

            is Result.Error -> {

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = trendingResult.message
                )

            }

            is Result.Success -> {

                val releasesResult = releases.await()
                val globalResult = global.await()
                val localResult = local.await()

                _uiState.value = HomeUiState(

                    trendingSongs = trendingResult.data,

                    newReleases = (releasesResult as? Result.Success)?.data ?: emptyList(),

                    globalPlaylists = (globalResult as? Result.Success)?.data ?: emptyList(),

                    localPlaylists = (localResult as? Result.Success)?.data ?: emptyList(),

                    isLoading = false
                )

                Log.d("HOME", "Trending: ${trendingResult}")
                Log.d("HOME", "Releases: ${releasesResult}")
                Log.d("HOME", "Global: ${globalResult}")
                Log.d("HOME", "Local: ${localResult}")
            }
        }
    }
}