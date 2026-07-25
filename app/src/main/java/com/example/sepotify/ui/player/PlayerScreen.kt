package com.example.sepotify.ui.player

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import coil3.asDrawable
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import com.example.sepotify.R
import com.example.sepotify.domain.model.DownloadState
import com.example.sepotify.ui.theme.Dimens
import com.example.sepotify.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.sin

@SuppressLint("ConfigurationScreenWidthHeight", "LocalContextResourcesRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val state by viewModel.playerState.collectAsState()
    val songLiked by viewModel.isLiked.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val downloadState by viewModel.downloadState.collectAsState()

    // Adaptive scaling (as before)
    val scaleFactor = remember(screenWidthDp) {
        (screenWidthDp / 400f).coerceIn(0.6f, 1.2f)
    }
    fun dp(value: Int): Dp = (value * scaleFactor).dp
    fun sp(value: Int): TextUnit = (value * scaleFactor).sp

    // --- Color extraction (unchanged) ---
    var dominantColor by remember { mutableStateOf(Color(0xFF121212)) }
    var isColorExtracted by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.currentSong?.coverUrl) {
        isColorExtracted = false
        val coverUrl = state.currentSong?.coverUrl
        if (!coverUrl.isNullOrEmpty()) {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val request = ImageRequest.Builder(context)
                        .data(coverUrl)
                        .allowHardware(false)
                        .build()
                    val drawable = when (val result = context.imageLoader.execute(request)) {
                        is SuccessResult -> result.image.asDrawable(context.resources)
                        is ErrorResult -> null
                    }
                    (drawable as? BitmapDrawable)?.bitmap
                }
                if (bitmap != null) {
                    Palette.from(bitmap).generate { palette ->
                        // Inside LaunchedEffect
                        val colorInt = palette?.getDominantColor(Color.Black.toArgb()) ?: Color.Black.toArgb()
                        dominantColor = Color(colorInt)
                        isColorExtracted = true
                    }
                } else {
                    dominantColor = Color(0xFF121212)
                    isColorExtracted = true
                }
            } catch (_: Exception) {
                dominantColor = Color(0xFF121212)
                isColorExtracted = true
            }
        } else {
            dominantColor = Color(0xFF121212)
            isColorExtracted = true
        }
    }

    val backgroundColor = if (isColorExtracted) dominantColor else Color(0xFF121212)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            backgroundColor,
            backgroundColor.copy(alpha = 0.8f),
            Color(0xFF121212)
        ),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )

    // --- Rotation (unchanged) ---
    var rotation by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(state.isPlaying) {
        while (state.isPlaying && isActive) {
            rotation += 0.5f
            delay(16)
        }
    }

    // --- Audio wave amplitude ---
    val waveAmplitude by remember { derivedStateOf { if (state.isPlaying) 1f else 0f } }

    // --- Sleep timer state ---
    var expanded by remember { mutableStateOf(false) }
    val timerOptions = listOf(5, 10, 15, 30)
    var selectedTimerMinutes by remember { mutableIntStateOf(0) }

    val horizontalPadding = if (screenWidthDp < 360) Dimens.spaceMedium else Dimens.spaceExtraLarge

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .systemBarsPadding()
                .padding(horizontal = horizontalPadding, vertical = Dimens.spaceMedium)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Top bar with actions ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.spaceSmall),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(dp(32))) {
                        Icon(
                            Icons.Outlined.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White,
                            modifier = Modifier.size(dp(20))
                        )
                    }

                    // Add to Playlist
                    IconButton(
                        onClick = {
                            viewModel.loadUserPlaylists()
                            showAddToPlaylistDialog = true
                        },
                        modifier = Modifier.size(dp(32))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = stringResource(R.string.add_to_playlist),
                            tint = Color.White,
                            modifier = Modifier.size(dp(20))
                        )
                    }

                    // Download
                    when (val ds = downloadState) {
                        is DownloadState.NotStarted, is DownloadState.Pending -> {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        val result = viewModel.downloadCurrentSong()
                                        snackbarHostState.showSnackbar(
                                            when (result) {
                                                is Result.Success -> result.data
                                                is Result.Error -> result.message
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.size(dp(32))
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = stringResource(R.string.download),
                                    tint = Color.White,
                                    modifier = Modifier.size(dp(20))
                                )
                            }
                        }
                        is DownloadState.Downloading -> {
                            IconButton(
                                enabled = false,
                                modifier = Modifier.size(dp(32)),
                                onClick = {}
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(Dimens.iconMedium),
                                    progress = { ds.progress },
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = Color.White.copy(alpha = 0.3f)
                                )
                            }
                        }
                        DownloadState.Completed -> {
                            IconButton(
                                enabled = false,
                                modifier = Modifier.size(dp(32)),
                                onClick = {}
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.downloaded),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(dp(20))
                                )
                            }
                        }
                        DownloadState.Failed -> {
                            IconButton(
                                onClick = {
                                    scope.launch { viewModel.downloadCurrentSong() }
                                },
                                modifier = Modifier.size(dp(32))
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = stringResource(R.string.retry_download),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(dp(20))
                                )
                            }
                        }
                    }

                    // Like
                    IconButton(
                        onClick = {
                            val songId = state.currentSong?.id ?: return@IconButton
                            if (songLiked) viewModel.unlikeSong(songId)
                            else viewModel.likeSong(songId)
                        },
                        modifier = Modifier.size(dp(32))
                    ) {
                        Icon(
                            imageVector = if (songLiked) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                            contentDescription = if (songLiked) stringResource(R.string.unlike) else stringResource(R.string.like),
                            tint = if (songLiked) Color.Red else Color.White,
                            modifier = Modifier.size(dp(20))
                        )
                    }

                    // Sleep timer
                    if (state.sleepTimerRemaining > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatTime(state.sleepTimerRemaining),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                            IconButton(
                                onClick = { viewModel.cancelSleepTimer() },
                                modifier = Modifier.size(dp(26))
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.cancel_sleep_timer),
                                    tint = Color.White,
                                    modifier = Modifier.size(Dimens.iconSmall)
                                )
                            }
                        }
                    } else {
                        Box {
                            IconButton(
                                onClick = { expanded = true },
                                modifier = Modifier.size(dp(32))
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = stringResource(R.string.sleep_timer),
                                    tint = Color.White,
                                    modifier = Modifier.size(dp(20))
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                timerOptions.forEach { minutes ->
                                    DropdownMenuItem(
                                        text = { Text("${minutes} minutes") },
                                        onClick = {
                                            viewModel.setSleepTimer(minutes)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dp(12)))

                // --- Album art (adaptive) ---
                val song = state.currentSong
                if (song != null) {
                    val albumSize = (screenWidthDp * Dimens.playerAlbumArtFraction).dp
                        .coerceIn(Dimens.playerAlbumArtMin, Dimens.playerAlbumArtMax)
                    Box(
                        modifier = Modifier
                            .size(albumSize)
                            .shadow(
                                elevation = Dimens.spaceExtraLarge,
                                shape = CircleShape,
                                clip = false
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .rotate(rotation)
                        ) {
                            AsyncImage(
                                model = song.coverUrl,
                                contentDescription = song.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // Center hole
                        Box(
                            modifier = Modifier
                                .size(albumSize * 0.15f)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.spaceLarge))

                    // Title & Artist
                    Text(
                        text = song.title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                    Text(
                        text = song.artists.joinToString(", ") { it.name },
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Dimens.spaceLarge))

                    // --- Audio Wave Visualizer (unchanged core) ---
                    AudioWaveVisualizer(
                        isPlaying = state.isPlaying,
                        amplitude = waveAmplitude,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(Dimens.playerWaveHeight)
                    )

                    Spacer(modifier = Modifier.height(Dimens.spaceLarge))

                    // --- Seek Slider ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Slider(
                            value = state.currentPosition.toFloat(),
                            onValueChange = { viewModel.seekTo(it.toLong()) },
                            valueRange = 0f..state.duration.coerceAtLeast(1).toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(state.currentPosition),
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = formatTime(state.duration),
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.spaceLarge))

                    // --- Playback Controls ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = viewModel::previous,
                            modifier = Modifier.size(Dimens.iconLarge * 1.5f)
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = stringResource(R.string.previous),
                                tint = Color.White,
                                modifier = Modifier.size(Dimens.playerControlIconSize)
                            )
                        }

                        IconButton(
                            onClick = viewModel::playPause,
                            modifier = Modifier.size(Dimens.iconLarge * 2f)
                        ) {
                            Icon(
                                if (state.isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = if (state.isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(Dimens.playerLargeControlSize)
                            )
                        }

                        IconButton(
                            onClick = viewModel::next,
                            modifier = Modifier.size(Dimens.iconLarge * 1.5f)
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = stringResource(R.string.next),
                                tint = Color.White,
                                modifier = Modifier.size(Dimens.playerControlIconSize)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.spaceLarge))

                    // --- Bottom controls (shuffle, speed, repeat) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle
                        IconButton(
                            onClick = viewModel::toggleShuffle,
                            modifier = Modifier.size(Dimens.iconLarge)
                        ) {
                            Icon(
                                imageVector = if (state.shuffle) Icons.Filled.Shuffle else Icons.Outlined.Shuffle,
                                contentDescription = stringResource(R.string.shuffle),
                                tint = if (state.shuffle) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.size(Dimens.iconMedium)
                            )
                        }

                        // Speed chips
                        val speeds = listOf(1f, 1.5f, 2f)
                        speeds.forEach { speed ->
                            val isSelected = kotlin.math.abs(state.playbackSpeed - speed) < 0.01f
                            Surface(
                                modifier = Modifier
                                    .padding(horizontal = Dimens.paddingExtraSmall)
                                    .clickable { viewModel.setPlaybackSpeed(speed) },
                                shape = RoundedCornerShape(Dimens.cardCornerRadius),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isSelected) Color.Black else Color.White
                            ) {
                                Text(
                                    text = if (speed % 1 == 0f) "${speed.toInt()}x" else "${speed}x",
                                    modifier = Modifier.padding(
                                        horizontal = Dimens.paddingMedium,
                                        vertical = Dimens.paddingSmall
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }

                        // Repeat
                        IconButton(
                            onClick = viewModel::toggleRepeat,
                            modifier = Modifier.size(Dimens.iconLarge)
                        ) {
                            Icon(
                                imageVector = when (state.repeatMode) {
                                    Player.REPEAT_MODE_OFF -> Icons.Outlined.Repeat
                                    Player.REPEAT_MODE_ALL -> Icons.Filled.Repeat
                                    else -> Icons.Filled.RepeatOne
                                },
                                contentDescription = stringResource(R.string.repeat),
                                tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.size(Dimens.iconMedium)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                } else {
                    // No song playing
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.nothing_playing),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // --- Add to Playlist Dialog (unchanged) ---
            if (showAddToPlaylistDialog) {
                AlertDialog(
                    onDismissRequest = { showAddToPlaylistDialog = false },
                    title = { Text(stringResource(R.string.add_to_playlist)) },
                    text = {
                        val playlists by viewModel.userPlaylists.collectAsState()
                        if (playlists.isEmpty()) {
                            Text(stringResource(R.string.no_playlists_yet))
                        } else {
                            LazyColumn {
                                items(playlists) { playlist ->
                                    TextButton(
                                        onClick = {
                                            viewModel.addCurrentSongToPlaylist(playlist.id)
                                            showAddToPlaylistDialog = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(playlist.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showAddToPlaylistDialog = false }) {
                            Text(stringResource(R.string.close))
                        }
                    }
                )
            }
        }
    }
}

// --- AudioWaveVisualizer (unchanged core) ---
@Composable
fun AudioWaveVisualizer(
    isPlaying: Boolean,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 30
    val density = LocalDensity.current.density
    val primaryColor = MaterialTheme.colorScheme.primary  // ← captured here

    val phase by animateFloatAsState(
        targetValue = if (isPlaying) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        finishedListener = {}
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = width / barCount
        val maxHeight = height * 0.8f

        for (i in 0 until barCount) {
            val x = i * barWidth + barWidth / 2
            val wave = sin(phase * 2 * Math.PI + (i / barCount.toFloat()) * 2 * Math.PI)
            val scale = if (isPlaying) (0.5f + 0.5f * wave.toFloat()) else 0.2f
            val barHeight = maxHeight * scale * amplitude.coerceAtLeast(0.2f)

            drawLine(
                color = if (isPlaying) primaryColor else Color.Gray,  // ← use captured variable
                start = Offset(x, (height - barHeight) / 2),
                end = Offset(x, (height + barHeight) / 2),
                strokeWidth = barWidth * 0.6f,
                cap = StrokeCap.Round
            )
        }
    }
}

// --- Helper: format time ---
@SuppressLint("DefaultLocale")
private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}