package com.example.sepotify.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sepotify.R
import com.example.sepotify.domain.model.Message
import com.example.sepotify.ui.components.SongShareCard
import com.example.sepotify.ui.theme.Dimens
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MessageBubble(
    message: Message,
    isMine: Boolean,
    onRetry: (Message) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spaceSmall, vertical = Dimens.paddingExtraSmall),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = Dimens.cardCornerRadius,
                topEnd = Dimens.cardCornerRadius,
                bottomStart = if (isMine) Dimens.cardCornerRadius else Dimens.paddingExtraSmall,
                bottomEnd = if (isMine) Dimens.paddingExtraSmall else Dimens.cardCornerRadius
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(Dimens.spaceMedium)) {
                // Song share card
                if (message.songId != null) {
                    SongShareCard(
                        songId = message.songId,
                        onPlay = { /* play song */ }
                    )
                }
                // Text content
                if (!message.content.isNullOrBlank()) {
                    Text(
                        text = message.content,
                        color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                // Status and timestamp
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatTime(message.sentAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                    if (isMine) {
                        when (message.getStatus()) {
                            Message.Status.SENDING -> {
                                Icon(Icons.Default.Schedule, contentDescription = stringResource(R.string.sending), modifier = Modifier.size(Dimens.iconSmall))
                                Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                                TextButton(
                                    onClick = { onRetry(message) },
                                    modifier = Modifier.height(Dimens.iconMedium)
                                ) {
                                    Text(stringResource(R.string.retry), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Message.Status.SENT -> {
                                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.sent), modifier = Modifier.size(Dimens.iconSmall))
                            }
                            Message.Status.READ -> {
                                Icon(Icons.Default.DoneAll, contentDescription = stringResource(R.string.read), modifier = Modifier.size(Dimens.iconSmall))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTime(time: Instant): String {
    return time
        .toJavaInstant()
        .atZone(ZoneId.systemDefault())
        .format(
            DateTimeFormatter.ofPattern("HH:mm")
        )
}