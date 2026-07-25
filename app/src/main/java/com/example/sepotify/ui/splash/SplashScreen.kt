package com.example.sepotify.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.sepotify.R
import com.example.sepotify.ui.theme.Dimens
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToMain: (String) -> Unit,
    splashViewModel: SplashViewModel = koinViewModel()
) {
    // Animation for logo scale
    val scale = remember { Animatable(0f) }
    // Animation for logo alpha (fade in)
    val alpha = remember { Animatable(0f) }

    val uiState by splashViewModel.splashUiState.collectAsState()

    // Run animations when screen first appears
    LaunchedEffect(Unit) {
        // Scale up with a custom easing
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = { t ->
                    val tMinusOne = t - 1.0f
                    tMinusOne * tMinusOne * (3f * tMinusOne + 2f) + 1.0f
                }
            )
        )
        // Fade in after scale starts
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    // Navigation logic – unchanged
    LaunchedEffect(uiState) {
        if (uiState !is SplashUiState.Loading) {
            delay(1000)
            when (val state = uiState) {
                is SplashUiState.Authenticated -> onNavigateToMain(state.userId)
                is SplashUiState.Unauthenticated -> onNavigateToSignIn()
                else -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha.value) // fade in the whole column
        ) {
            // Logo circle
            Box(
                modifier = Modifier
                    .size(Dimens.avatarExtraLarge * 1.5f) // ~144dp
                    .scale(scale.value)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.app_name_initial), // "S"
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displayLarge, // large, bold
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spaceExtraLarge))

            Text(
                text = stringResource(R.string.app_name), // "Sepotify"
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Dimens.spaceExtraLarge * 2))

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(Dimens.iconExtraLarge)
            )
        }
    }
}