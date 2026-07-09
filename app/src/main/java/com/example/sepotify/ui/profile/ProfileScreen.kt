package com.example.sepotify.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import com.example.sepotify.R
import com.example.sepotify.ui.theme.AppShape
import com.example.sepotify.ui.theme.Dimens
import com.example.sepotify.ui.theme.GoldenYellow
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onLogout: () -> Unit,
    onDeleted: () -> Unit, // kept for compatibility, but no delete button now
    onFollowListClick: (String, String) -> Unit
) {
    val state by viewModel.profileState.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()
    val followersCount by viewModel.followersCount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.myProfile()
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = Dimens.cardElevation,
                color = MaterialTheme.colorScheme.background
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.profile_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.paddingDoubleExtra)
        ) {
            when (val uiState = state) {
                is ProfileUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimens.iconExtraLarge),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                is ProfileUiState.Success -> {
                    val profile = uiState.profile

                    // Image picker launcher
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let {
                            viewModel.uploadAvatar(it)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
                    ) {
                        // --- Avatar with edit icon (always visible) ---
                        Box(
                            modifier = Modifier
                                .size(Dimens.avatarExtraLarge + Dimens.iconLarge)
                                .padding(Dimens.iconLarge / 2),
                            contentAlignment = Alignment.Center
                        ) {
                            // Avatar image
                            Box(
                                modifier = Modifier
                                    .size(Dimens.avatarExtraLarge)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                AsyncImage(
                                    model = profile.avatarUrl,
                                    placeholder = painterResource(R.drawable.ic_profile_placeholder),
                                    error = painterResource(R.drawable.ic_profile_placeholder),
                                    contentDescription = stringResource(R.string.profile_avatar_desc),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                            }

                            // Edit button – opens image picker
                            IconButton(
                                onClick = { launcher.launch("image/*") },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(Dimens.iconLarge)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = Dimens.borderWidthSmall,
                                        color = MaterialTheme.colorScheme.background,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit_avatar),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(Dimens.iconSmall)
                                )
                            }
                        }

                        // --- Name & Username ---
                        Text(
                            text = profile.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "@${profile.username}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // --- Premium Badge or Upgrade Button ---
                        if (profile.isPremium) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = AppShape.SmallChip,
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
                        } else {
                            Button(
                                onClick = { viewModel.upgradeToPremium() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldenYellow
                                ),
                                shape = AppShape.Button,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimens.buttonHeight)
                            ) {
                                Text(
                                    text = stringResource(R.string.upgrade_premium),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        // --- Stats Row (Followers / Following) ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.spaceSmall),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(
                                onClick = { onFollowListClick(viewModel.currentProfileId ?: "", "followers") },
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
                                onClick = { onFollowListClick(viewModel.currentProfileId ?: "", "following") },
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

                        Spacer(modifier = Modifier.weight(1f))

                        // --- Logout Button ---
                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = AppShape.Button,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.buttonHeight)
                        ) {
                            Text(
                                text = stringResource(R.string.logout),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                    }
                }

                is ProfileUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                        Button(
                            onClick = { viewModel.myProfile() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.retry),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                is ProfileUiState.Deleted -> {
                    Text(
                        text = stringResource(R.string.account_deleted),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}