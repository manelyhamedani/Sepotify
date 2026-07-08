package com.example.sepotify.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Base font sizes (medium scale)
private val defaultFontSize = 16.sp

// Helper to scale sizes
private fun scaleFontSize(base: Float, scale: Float) = (base * scale).sp

// Common text styles with scaling factor
private fun createTypography(scale: Float): Typography {
    val h1 = scaleFontSize(32f, scale)
    val h2 = scaleFontSize(28f, scale)
    val h3 = scaleFontSize(24f, scale)
    val h4 = scaleFontSize(20f, scale)
    val h5 = scaleFontSize(18f, scale)
    val h6 = scaleFontSize(16f, scale)
    val body1 = scaleFontSize(16f, scale)
    val body2 = scaleFontSize(14f, scale)
    val button = scaleFontSize(14f, scale)
    val caption = scaleFontSize(12f, scale)
    val overline = scaleFontSize(10f, scale)

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = h1,
            lineHeight = h1 * 1.2f
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = h2,
            lineHeight = h2 * 1.2f
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = h3,
            lineHeight = h3 * 1.2f
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = h4,
            lineHeight = h4 * 1.2f
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = h5,
            lineHeight = h5 * 1.2f
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = h6,
            lineHeight = h6 * 1.2f
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = body1,
            lineHeight = body1 * 1.5f
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = body2,
            lineHeight = body2 * 1.5f
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = caption,
            lineHeight = caption * 1.5f
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = button,
            lineHeight = button * 1.2f
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = caption,
            lineHeight = caption * 1.2f
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = overline,
            lineHeight = overline * 1.2f
        )
    )
}

// Available font size presets
val SmallTypography = createTypography(0.85f)
val MediumTypography = createTypography(1.0f)
val LargeTypography = createTypography(1.15f)

// Default (will be selected based on user preference)
val AppTypography = MediumTypography