package com.example.sepotify.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.example.sepotify.R
import com.example.sepotify.data.local.database.entity.SearchHistory
import com.example.sepotify.ui.theme.AppShape
import com.example.sepotify.ui.theme.Dimens
import com.example.sepotify.ui.theme.unselectedIcon
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    userId: String,
    onBack: () -> Unit,
    onResultClick: (SearchResultItem) -> Unit,
    viewModel: SearchViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val query by viewModel.query.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBar(
                        query = query,
                        onQueryChange = viewModel::updateQuery,
                        onClear = { viewModel.updateQuery("") }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.spaceLarge)
        ) {
            // Filter chips (scrollable row)
            FilterChipsRow(
                selectedFilter = selectedFilter,
                onFilterSelected = viewModel::updateFilter
            )

            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            // Content based on query & UI state
            when {
                query.isEmpty() -> {
                    HistorySection(
                        history = searchHistory,
                        onDelete = viewModel::deleteHistoryItem,
                        onClearAll = viewModel::clearAllHistory,
                        onHistoryClick = viewModel::updateQuery
                    )
                }
                else -> {
                    when (uiState) {
                        is SearchUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(Dimens.iconExtraLarge)
                                )
                            }
                        }
                        is SearchUiState.Empty -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = unselectedIcon),
                                        modifier = Modifier.size(Dimens.iconExtraLarge)
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                                    Text(
                                        text = stringResource(R.string.search_no_results),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        is SearchUiState.Sectioned -> {
                            SectionedResults(
                                sections = (uiState as SearchUiState.Sectioned).sections,
                                onResultClick = onResultClick
                            )
                        }
                        is SearchUiState.Paging -> {
                            PagingResults(
                                pagingFlow = (uiState as SearchUiState.Paging).pagingDataFlow,
                                onResultClick = onResultClick
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------- Scrollable Filter Chips ----------
@Composable
fun FilterChipsRow(
    selectedFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit
) {
    val filters = listOf(
        FilterType.ALL to stringResource(R.string.filter_all),
        FilterType.SONGS to stringResource(R.string.filter_songs),
        FilterType.ARTISTS to stringResource(R.string.filter_artists),
        FilterType.PLAYLISTS to stringResource(R.string.filter_playlists),
        FilterType.PROFILES to stringResource(R.string.filter_profiles)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(filters) { (filter, label) ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(label) },
                modifier = Modifier.height(Dimens.chipHeight),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(Dimens.cardCornerRadius)
            )
        }
    }
}

// ---------- Search Bar ----------
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(R.string.search_hint),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium // reduce font size if needed
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(Dimens.searchBarCornerRadius),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

// ---------- History Section ----------
@Composable
fun HistorySection(
    history: List<SearchHistory>,
    onDelete: (SearchHistory) -> Unit,
    onClearAll: () -> Unit,
    onHistoryClick: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.recent_searches),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onClearAll) {
                Text(
                    text = stringResource(R.string.clear_all),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingDoubleExtra),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_recent_searches),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(history) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHistoryClick(item.query) }
                            .padding(vertical = Dimens.spaceSmall),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.query,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onDelete(item) },
                            modifier = Modifier.size(Dimens.iconMedium)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(Dimens.iconSmall)
                            )
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }
        }
    }
}

// ---------- Sectioned Results (ALL filter) ----------
@Composable
fun SectionedResults(
    sections: List<ResultSection>,
    onResultClick: (SearchResultItem) -> Unit
) {
    LazyColumn {
        sections.forEach { section ->
            item {
                SectionHeader(
                    title = section.title,
                    onSeeAll = section.seeAllAction,
                    showSeeAll = section.items.isNotEmpty()
                )
            }
            items(section.items) { resultItem ->
                ResultItem(resultItem, onResultClick)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit, showSeeAll: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spaceSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (showSeeAll) {
            TextButton(onClick = onSeeAll) {
                Text(
                    text = stringResource(R.string.see_all),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ---------- Paging Results (individual filters) ----------
@Composable
fun PagingResults(
    pagingFlow: Flow<PagingData<SearchResultItem>>,
    onResultClick: (SearchResultItem) -> Unit
) {
    val pagingItems = pagingFlow.collectAsLazyPagingItems()

    LazyColumn {
        // Iterate loaded items
        for (index in 0 until pagingItems.itemCount) {
            val resultItem = pagingItems[index]
            if (resultItem != null) {
                item {
                    ResultItem(resultItem, onResultClick)
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }
        }

        // Loading / error states
        when {
            pagingItems.loadState.refresh is LoadState.Loading -> {
                item { LoadingIndicator() }
            }
            pagingItems.loadState.append is LoadState.Loading -> {
                item { LoadingIndicator() }
            }
            pagingItems.loadState.refresh is LoadState.Error -> {
                item {
                    Text(
                        text = stringResource(R.string.load_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(Dimens.spaceMedium)
                    )
                }
            }
            pagingItems.loadState.append is LoadState.Error -> {
                item {
                    Text(
                        text = stringResource(R.string.load_more_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(Dimens.spaceMedium)
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.spaceMedium),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.iconMedium)
        )
    }
}

// ---------- Single Result Item ----------
private data class ResultItemData(
    val title: String,
    val subtitle: String,
    val imageUrl: String?,
    val type: String
)

@Composable
fun ResultItem(item: SearchResultItem, onClick: (SearchResultItem) -> Unit) {
    val data = when (item) {
        is SearchResultItem.Song -> ResultItemData(
            title = item.song.title,
            subtitle = item.song.artists.joinToString(", ") { it.name },
            imageUrl = item.song.coverUrl,
            type = stringResource(R.string.type_song)
        )
        is SearchResultItem.Artist -> ResultItemData(
            title = item.artist.name,
            subtitle = item.artist.bio ?: "",
            imageUrl = item.artist.avatarUrl,
            type = stringResource(R.string.type_artist)
        )
        is SearchResultItem.Playlist -> ResultItemData(
            title = item.playlist.name,
            subtitle = item.playlist.description ?: "",
            imageUrl = item.playlist.coverUrl,
            type = stringResource(R.string.type_playlist)
        )
        is SearchResultItem.Profile -> ResultItemData(
            title = item.profile.fullName,
            subtitle = "@${item.profile.username}",
            imageUrl = item.profile.avatarUrl,
            type = stringResource(R.string.type_profile)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) }
            .padding(vertical = Dimens.spaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with placeholder
        AsyncImage(
            model = data.imageUrl,
            placeholder = painterResource(getPlaceholderForType(item)),
            error = painterResource(getPlaceholderForType(item)),
            contentDescription = null,
            modifier = Modifier
                .size(Dimens.resultItemAvatarSize)
                .clip(RoundedCornerShape(Dimens.cardCornerRadius))
        )

        Spacer(modifier = Modifier.width(Dimens.spaceMedium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Type tag
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(Dimens.cardCornerRadius)
        ) {
            Text(
                text = data.type,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = Dimens.paddingSmall,
                    vertical = Dimens.resultItemTypeTagPadding
                )
            )
        }
    }
}

// Helper to choose placeholder drawable
@Composable
private fun getPlaceholderForType(item: SearchResultItem): Int {
    return when (item) {
        is SearchResultItem.Song -> R.drawable.ic_song_placeholder
        is SearchResultItem.Artist -> R.drawable.ic_profile_placeholder
        is SearchResultItem.Playlist -> R.drawable.ic_playlist_placeholder
        is SearchResultItem.Profile -> R.drawable.ic_profile_placeholder
    }
}