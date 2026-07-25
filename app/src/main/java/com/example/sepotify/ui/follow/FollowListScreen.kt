package com.example.sepotify.ui.follow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.sepotify.R
import com.example.sepotify.ui.theme.Dimens
import com.example.sepotify.ui.theme.Dimens.iconExtraLarge
import com.example.sepotify.ui.theme.Dimens.iconMedium
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(
    userId: String,
    currentUserId: String,
    mode: FollowListViewModel.FollowListMode,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit, // NEW: navigate to user profile
    viewModel: FollowListViewModel = koinViewModel(
        parameters = { parametersOf(userId, currentUserId, mode) }
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (mode == FollowListViewModel.FollowListMode.FOLLOWING)
                            stringResource(R.string.following_title)
                        else
                            stringResource(R.string.followers_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.paddingDoubleExtra)
        ) {
            when (uiState) {
                is FollowListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(iconExtraLarge)
                        )
                    }
                }

                is FollowListUiState.Success -> {
                    val pagingItems = (uiState as FollowListUiState.Success).pagingDataFlow
                        .collectAsLazyPagingItems()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                    ) {
                        val itemCount = pagingItems.itemCount
                        if (itemCount == 0 && pagingItems.loadState.refresh !is LoadState.Loading) {
                            // Empty state
                            item {
                                EmptyFollowState(
                                    mode = mode,
                                    modifier = Modifier.fillParentMaxSize()
                                )
                            }
                        } else {
                            for (index in 0 until itemCount) {
                                val profile = pagingItems[index]
                                if (profile != null) {
                                    item {
                                        FollowProfileItem(
                                            profile = profile,
                                            showUnfollow = currentUserId != profile.id,
                                            onUnfollow = { viewModel.unfollow(profile.id) },
                                            onClick = { onProfileClick(profile.id) } // ✅ navigation
                                        )
                                    }
                                }
                            }
                        }

                        // Loading/error states for paging
                        when {
                            pagingItems.loadState.refresh is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(iconMedium),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            pagingItems.loadState.append is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(Dimens.iconSmall),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
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
                        }
                    }
                }

                is FollowListUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stringResource(R.string.error)}: ${(uiState as FollowListUiState.Error).message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

// --- Empty state composable ---
@Composable
private fun EmptyFollowState(
    mode: FollowListViewModel.FollowListMode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (mode == FollowListViewModel.FollowListMode.FOLLOWING)
                Icons.Outlined.PersonAdd
            else
                Icons.Outlined.People,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(Dimens.iconExtraLarge)
        )
        Spacer(modifier = Modifier.height(Dimens.spaceMedium))
        Text(
            text = if (mode == FollowListViewModel.FollowListMode.FOLLOWING)
                stringResource(R.string.no_following)
            else
                stringResource(R.string.no_followers),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}