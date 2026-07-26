package com.example.sepotify.ui.chat

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.sepotify.ui.theme.Dimens

@Composable
fun TypingIndicator() {
    Log.d("ChatScreen", "TypingIndicator rendered")
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typingAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingExtraSmall),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(Dimens.cardCornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = Dimens.typingIndicatorMaxWidth)
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = Dimens.paddingLarge,
                    vertical = Dimens.paddingMedium
                ),
                horizontalArrangement = Arrangement.spacedBy(Dimens.typingDotSpacing)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(Dimens.typingDotSize)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}