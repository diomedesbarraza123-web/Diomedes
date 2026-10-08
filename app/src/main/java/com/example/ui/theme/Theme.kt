package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SneakerDarkColorScheme = darkColorScheme(
    primary = SneakerOrange,
    onPrimary = Color.White,
    primaryContainer = SneakerOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = MintVerified,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00381C),
    onSecondaryContainer = MintVerified,
    tertiary = PaypalBlue,
    onTertiary = Color.White,
    background = ObsidianBackground,
    onBackground = SneakerTextPrimary,
    surface = ObsidianSurface,
    onSurface = SneakerTextPrimary,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = SneakerTextSecondary,
    outline = SneakerBorder
)

private val SneakerLightColorScheme = lightColorScheme(
    primary = SneakerOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = SneakerOrangeDark,
    secondary = MintVerified,
    onSecondary = Color.White,
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFE5E7EB)
)

@Composable
fun SneakerAuctionTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode for sneaker culture
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SneakerDarkColorScheme else SneakerLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
