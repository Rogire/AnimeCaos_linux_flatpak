package com.animecaos.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AnimeCaosTokens {
    val BackgroundTop = Color(0xFF080A0F)
    val BackgroundBottom = Color(0xFF121723)
    val SurfaceGlass = Color(0x1AF5F7FF)
    val SurfaceGlassStrong = Color(0x26F5F7FF)
    val BorderGlass = Color(0x33F5F7FF)
    val BorderFocus = Color(0x50C52B37)
    val SecondaryRed = Color(0xFF9A1E28)
    val SecondaryRedSoft = Color(0xFFBC2E3A)
    val SecondaryRedGlow = Color(0x66BC2E3A)
    val TextPrimary = Color(0xFFE9EDF6)
    val TextMuted = Color(0xFF98A1B5)
    val TextOnRed = Color(0xFFFFF2F4)
    val DividerSoft = Color(0x22FFFFFF)

    val AppBackgroundBrush = Brush.verticalGradient(
        listOf(BackgroundTop, BackgroundBottom),
    )
}

private val AnimeCaosColorScheme = darkColorScheme(
    primary = AnimeCaosTokens.TextPrimary,
    onPrimary = AnimeCaosTokens.BackgroundTop,
    secondary = AnimeCaosTokens.SecondaryRedSoft,
    onSecondary = AnimeCaosTokens.TextOnRed,
    background = AnimeCaosTokens.BackgroundTop,
    onBackground = AnimeCaosTokens.TextPrimary,
    surface = AnimeCaosTokens.SurfaceGlass,
    onSurface = AnimeCaosTokens.TextPrimary,
    surfaceVariant = AnimeCaosTokens.SurfaceGlassStrong,
    onSurfaceVariant = AnimeCaosTokens.TextMuted,
    error = Color(0xFFFF6E7A),
    onError = Color(0xFF1B0E12),
)

private val AnimeCaosTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    ),
)

@Composable
fun AnimeCaosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AnimeCaosColorScheme,
        typography = AnimeCaosTypography,
        content = content,
    )
}
