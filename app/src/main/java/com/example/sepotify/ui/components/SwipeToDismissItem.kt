package com.example.sepotify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.sepotify.R
import com.example.sepotify.domain.model.Song
import com.example.sepotify.ui.theme.Dimens

@Composable
fun SwipeToDismissSongItem(
    song: Song,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.25f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = Dimens.spaceLarge),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        modifier = modifier
    ) {
        SongItem(
            song = song,
            onClick = { onClick() }
        )
    }
}

//package com.example.sepotify.ui.components
//
//import androidx.compose.animation.animateColorAsState
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.outlined.Delete
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.unit.dp
//import com.example.sepotify.domain.model.Song
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SwipeToDismissItem(
//    song: Song,
//    modifier: Modifier = Modifier,
//    onRemove: () -> Unit,
//    onClick: (Song) -> Unit
//) {
//
//    val dismissState = rememberSwipeToDismissBoxState(
//        positionalThreshold = { it * .35f },
//        confirmValueChange = {
//            it == SwipeToDismissBoxValue.EndToStart
//        }
//    )
//
//    LaunchedEffect(dismissState.currentValue) {
//        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
//            onRemove()
//            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
//        }
//    }
//
//    val progress =
//        dismissState.progress.coerceIn(0f,1f)
//
//    val background by animateColorAsState(
//        if(progress > .1f)
//            MaterialTheme.colorScheme.errorContainer
//        else
//            MaterialTheme.colorScheme.surface,
//        label = ""
//    )
//
//    SwipeToDismissBox(
//        modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp),
//        state = dismissState,
//        enableDismissFromStartToEnd = false,
//        enableDismissFromEndToStart = true,
//
//        backgroundContent = {
//
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .clip(RoundedCornerShape(18.dp))
//                    .background(background)
//                    .padding(end = 28.dp),
//                contentAlignment = Alignment.CenterEnd
//            ) {
//
//                Icon(
//                    imageVector = Icons.Outlined.Delete,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.onErrorContainer,
//                    modifier = Modifier.graphicsLayer {
//                        scaleX = .8f + progress * .2f
//                        scaleY = .8f + progress * .2f
//                        alpha = progress
//                    }
//                )
//
//            }
//
//        }
//
//    ) {
//
//        SongItem(
//            song = song,
//            onClick = onClick
//        )
//
//    }
//
//}