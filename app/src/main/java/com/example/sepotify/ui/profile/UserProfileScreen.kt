package com.example.sepotify.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.example.sepotify.R
import com.example.sepotify.domain.model.Song
import com.example.sepotify.ui.components.FollowButton
import com.example.sepotify.ui.components.PlaylistItem
import com.example.sepotify.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    currentUserId: String,
    onBack: () -> Unit,
    onPlaylistClick: (String) -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    onChatClick: (String) -> Unit,
    onFollowListClick: (String, String) -> Unit,
    viewModel: UserProfileViewModel = koinViewModel(
        parameters = { parametersOf(userId, currentUserId) }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val followState by viewModel.followState.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()
    val followersCount by viewModel.followersCount.collectAsState()
    val publicPlaylistItems = viewModel.publicPlaylistsFlow.collectAsLazyPagingItems()

    LaunchedEffect(userId) {
        viewModel.syncFollows()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.user_profile_title),
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
                actions = {
                    if (userId != currentUserId) {
                        IconButton(onClick = { onChatClick(userId) }) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = stringResource(R.string.message)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (uiState) {
                is UserProfileUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.iconExtraLarge)
                        )
                    }
                }

                is UserProfileUiState.Success -> {
                    val profile = (uiState as UserProfileUiState.Success).profile

                    // Use a Column with fillMaxSize, and the LazyColumn will take remaining space
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimens.paddingDoubleExtra)
                    ) {
                        // --- Profile header (non‑scrollable) ---
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(Dimens.avatarXXLarge)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            if (profile.avatarUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = profile.avatarUrl,
                                    contentDescription = stringResource(R.string.profile_avatar_desc),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(Dimens.paddingLarge)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

                        Text(
                            text = profile.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "@${profile.username}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        if (profile.isPremium) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = Dimens.spaceSmall)
                            ) {
                                Text(
                                    text = stringResource(R.string.premium_badge),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        horizontal = Dimens.paddingLarge,
                                        vertical = Dimens.paddingSmall
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.spaceSmall))

                        // Stats row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.spaceMedium),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(
                                onClick = { onFollowListClick(userId, "followers") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$followersCount",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = stringResource(R.string.followers_label),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            TextButton(
                                onClick = { onFollowListClick(userId, "following") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$followingCount",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = stringResource(R.string.following_label),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Follow button
                        if (userId != currentUserId) {
                            FollowButton(
                                isFollowing = followState.isFollowing,
                                isLoading = followState.isLoading,
                                onFollow = { viewModel.follow(userId, currentUserId) },
                                onUnfollow = { viewModel.unfollow(userId, currentUserId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.spaceLarge)
                            )
                            Spacer(modifier = Modifier.height(Dimens.spaceLarge))
                        }

                        // --- Playlists section (scrollable) ---
                        Text(
                            text = stringResource(R.string.public_playlists_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = Dimens.spaceLarge)
                        )

                        Spacer(modifier = Modifier.height(Dimens.spaceLarge))

                        // LazyColumn fills remaining space
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            val loadState = publicPlaylistItems.loadState
                            when {
                                loadState.refresh is LoadState.Loading -> {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(Dimens.iconMedium),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                loadState.refresh is LoadState.Error -> {
                                    item {
                                        Text(
                                            text = stringResource(R.string.load_error),
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(Dimens.spaceMedium)
                                        )
                                    }
                                }
                                else -> {
                                    val itemCount = publicPlaylistItems.itemCount
                                    if (itemCount == 0) {
                                        // Empty state
                                        item {
                                            EmptyPlaylistState()
                                        }
                                    } else {
                                        // Display items using for‑loop over indices
                                        for (index in 0 until itemCount) {
                                            val playlist = publicPlaylistItems[index]
                                            if (playlist != null) {
                                                item {
                                                    PlaylistItem(playlist) {
                                                        onPlaylistClick(playlist.id)
                                                    }
                                                    Divider()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is UserProfileUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stringResource(R.string.error)}: ${(uiState as UserProfileUiState.Error).message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPlaylistState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.paddingDoubleExtra),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.LibraryMusic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(Dimens.iconExtraLarge)
        )
        Spacer(modifier = Modifier.height(Dimens.spaceMedium))
        Text(
            text = stringResource(R.string.no_public_playlists),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.empty_playlist_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}