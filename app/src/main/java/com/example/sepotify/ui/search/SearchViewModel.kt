package com.example.sepotify.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.sepotify.data.local.database.dao.SearchHistoryDao
import com.example.sepotify.data.local.database.entity.SearchHistory
import com.example.sepotify.data.repository.MusicRepository
import com.example.sepotify.data.repository.ProfileRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.paging.map
import com.example.sepotify.domain.model.Artist
import com.example.sepotify.domain.model.Playlist
import com.example.sepotify.domain.model.Profile
import com.example.sepotify.domain.model.Song
import kotlinx.coroutines.flow.first


sealed class FilterType {
    object ALL : FilterType()
    object SONGS : FilterType()
    object ARTISTS : FilterType()
    object PLAYLISTS : FilterType()
    object PROFILES : FilterType()
}

data class ResultSection(
    val title: String,
    val items: List<SearchResultItem>,
    val seeAllAction: () -> Unit
)

sealed class SearchUiState {
    object Empty : SearchUiState()
    object Loading : SearchUiState()
    data class Sectioned(val sections: List<ResultSection>) : SearchUiState()
    data class Paging(val pagingDataFlow: Flow<PagingData<SearchResultItem>>) : SearchUiState()
}

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val musicRepo: MusicRepository,
    private val profileRepo: ProfileRepository,
    private val searchHistoryDao: SearchHistoryDao,
    private val userId: String
): ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedFilter = MutableStateFlow<FilterType>(FilterType.ALL)
    val selectedFilter: StateFlow<FilterType> = _selectedFilter.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val searchHistory: Flow<List<SearchHistory>> = searchHistoryDao.getAll(userId)

    init {
        viewModelScope.launch {
            _query
                .debounce(500)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        performSearch(query, _selectedFilter.value)
                        searchHistoryDao.insert(SearchHistory(userId = userId, query = query))
                    }
                    else {
                        _uiState.value = SearchUiState.Empty
                    }
                }
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun updateFilter(filter: FilterType) {
        _selectedFilter.value = filter
        val currentQuery = _query.value
        if (currentQuery.isNotBlank()) {
            performSearch(currentQuery, filter)
        }
        else {
            _uiState.value = SearchUiState.Empty
        }
    }

    fun seeAllInCategory(category: FilterType) {
        updateFilter(category)
    }

    fun deleteHistoryItem(history: SearchHistory) {
        viewModelScope.launch {
            searchHistoryDao.delete(history)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            searchHistoryDao.clearAll(userId)
        }
    }

    private fun MutableList<ResultSection>.addSongsSection(songs: List<Song>?) {
        songs?.takeIf { it.isNotEmpty() }?.let { it ->
            add(
                ResultSection(
                    title = "Songs",
                    items = it.map { SearchResultItem.Song(it) },
                    seeAllAction = { seeAllInCategory(FilterType.SONGS) }
                )
            )
        }
    }

    private fun MutableList<ResultSection>.addArtistsSection(artists: List<Artist>?) {
        artists?.takeIf { it.isNotEmpty() }?.let { it ->
            add(
                ResultSection(
                    title = "Artists",
                    items = it.map { SearchResultItem.Artist(it) },
                    seeAllAction = { seeAllInCategory(FilterType.ARTISTS) }
                )
            )
        }
    }

    private fun MutableList<ResultSection>.addPlaylistsSection(playlists: List<Playlist>?) {
        playlists?.takeIf { it.isNotEmpty() }?.let { it ->
            add(
                ResultSection(
                    title = "Playlists",
                    items = it.map { SearchResultItem.Playlist(it) },
                    seeAllAction = { seeAllInCategory(FilterType.PLAYLISTS) }
                )
            )
        }
    }

    private fun MutableList<ResultSection>.addProfilesSection(profiles: List<Profile>?) {
        profiles?.takeIf { it.isNotEmpty() }?.let { it ->
            add(
                ResultSection(
                    title = "Profiles",
                    items = it.map { SearchResultItem.Profile(it) },
                    seeAllAction = { seeAllInCategory(FilterType.PROFILES) }
                )
            )
        }
    }

    private fun performSearch(query: String, filter: FilterType) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                when (filter) {
                    FilterType.ALL -> {
                        val songsDeferred = async { musicRepo.searchSongsLimited(query) }
                        val artistsDeferred = async { musicRepo.searchArtistsLimited(query) }
                        val playlistsDeferred = async { musicRepo.searchPlaylistsLimited(query) }
                        val profilesDeferred = async { profileRepo.searchProfilesLimited(query) }

                        val songs = songsDeferred.await()
                        val artists = artistsDeferred.await()
                        val playlists = playlistsDeferred.await()
                        val profiles = profilesDeferred.await()

                        val sections = buildList {
                            addSongsSection(songs.getOrNull())
                            addArtistsSection(artists.getOrNull())
                            addPlaylistsSection(playlists.getOrNull())
                            addProfilesSection(profiles.getOrNull())
                        }.filter { it.items.isNotEmpty() }

                        _uiState.value = if (sections.isEmpty()) {
                            SearchUiState.Empty  // or show "no results"
                        } else {
                            SearchUiState.Sectioned(sections)
                        }
                    }

                    FilterType.SONGS -> {
                        val pagingFlow = musicRepo.searchSongs(query)
                            .map { pagingData ->
                                pagingData.map { song -> SearchResultItem.Song(song) as SearchResultItem }
                            }
                        _uiState.value = SearchUiState.Paging(pagingFlow)
                    }

                    FilterType.ARTISTS -> {
                        val pagingFlow = musicRepo.searchArtists(query)
                            .map { pagingData ->
                                pagingData.map { artist -> SearchResultItem.Artist(artist) as SearchResultItem }
                            }
                        _uiState.value = SearchUiState.Paging(pagingFlow)
                    }

                    FilterType.PLAYLISTS -> {
                        val pagingFlow = musicRepo.searchPlaylists(query)
                            .map { pagingData ->
                                pagingData.map { playlist -> SearchResultItem.Playlist(playlist) as SearchResultItem }
                            }
                        _uiState.value = SearchUiState.Paging(pagingFlow)
                    }

                    FilterType.PROFILES -> {
                        val pagingFlow = profileRepo.searchProfiles(query)
                            .map { pagingData ->
                                pagingData.map { profile -> SearchResultItem.Profile(profile) as SearchResultItem }
                            }
                        _uiState.value = SearchUiState.Paging(pagingFlow)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Empty
            }
        }
    }
}