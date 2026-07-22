package com.example.sepotify.ui.chat

import android.util.Log
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.example.sepotify.R
import com.example.sepotify.data.remote.repository.SyncState
import com.example.sepotify.ui.theme.Dimens
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUserId: String,
    otherUserId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = koinViewModel(
        parameters = { parametersOf(currentUserId, otherUserId) }
    )
) {
    val messagesFlow = viewModel.messageFlow
    val syncState by viewModel.syncState.collectAsState()
    val pagingItems = messagesFlow.collectAsLazyPagingItems()
    val isTyping by viewModel.isTyping.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val partnerProfile by viewModel.partnerProfile.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Start real-time for messages (already started in repository)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when(event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    viewModel.clearChatPartner()
                }
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.markMessagesAsRead()
                    pagingItems.refresh()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.markMessagesAsRead()
        pagingItems.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Partner profile row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Avatar
                        Surface(
                            modifier = Modifier
                                .size(Dimens.iconLarge)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            if (!partnerProfile?.avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = partnerProfile?.avatarUrl,
                                    contentDescription = null,
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
                                        .padding(Dimens.paddingSmall)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = partnerProfile?.fullName ?: stringResource(R.string.unknown_user),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            // Optional: online status indicator could go here
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_chat))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimens.spaceMedium,
                        vertical = Dimens.spaceSmall
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        viewModel.setTyping(it.isNotEmpty())
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.type_message)) },
                    singleLine = true,
                    shape = RoundedCornerShape(Dimens.cardCornerRadius * 2),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                            viewModel.setTyping(false)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(Dimens.iconLarge * 1.5f)
                ) {
                    Icon(Icons.Default.Send, contentDescription = stringResource(R.string.send))
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {

            when (syncState) {
                is SyncState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                            Text(stringResource(R.string.loading_messages))
                        }
                    }
                }

                is SyncState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.sync_error, (syncState as SyncState.Error).message),
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                            Button(onClick = { viewModel.retrySync() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }

                is SyncState.Done -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true
                    ) {
                        // Typing indicator (shown at bottom, so appears first in reverse)
                        if (isTyping) {
                            item { TypingIndicator() }
                        }

                        for (index in 0 until pagingItems.itemCount) {
                            val msg = pagingItems[index]
                            if (msg != null) {
                                item {
                                    MessageBubble(
                                        message = msg,
                                        isMine = msg.senderId == currentUserId,
                                        onRetry = { viewModel.retrySending(it) }
                                    )
                                }
                            }
                        }

                        // Loading/error states
                        when {
                            pagingItems.loadState.refresh is LoadState.Loading -> {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(Dimens.iconMedium))
                                    }
                                }
                            }
                            pagingItems.loadState.refresh is LoadState.Error -> {
                                item {
                                    Text(
                                        text = stringResource(R.string.load_error),
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(Dimens.spaceMedium)
                                    )
                                }
                            }
                            pagingItems.loadState.append is LoadState.Loading -> {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(Dimens.iconMedium))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_conversation_title)) },
            text = { Text(stringResource(R.string.delete_conversation_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteConversation { onBack() }
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}