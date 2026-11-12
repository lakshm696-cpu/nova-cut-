package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NovaCutProDarkColorScheme = darkColorScheme(
    primary = NovaGold,
    onPrimary = Color(0xFF1A1405),
    primaryContainer = Color(0xFF3A2E14),
    onPrimaryContainer = NovaGoldLight,
    secondary = NovaPurple,
    onSecondary = Color(0xFF140B2E),
    secondaryContainer = Color(0xFF2D1E59),
    onSecondaryContainer = NovaPurpleLight,
    tertiary = NovaGoldLight,
    onTertiary = Color(0xFF1A1405),
    tertiaryContainer = Color(0xFF2B233D),
    onTertiaryContainer = Color(0xFFEDE4FF),
    background = NovaObsidian,
    onBackground = NovaTextPrimary,
    surface = NovaCharcoal,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaSurface,
    onSurfaceVariant = NovaTextSecondary,
    surfaceContainerHigh = NovaSurfaceElevated,
    outline = NovaBorder,
    error = NovaCoralRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NovaCutProDarkColorScheme,
        typography = Typography,
        content = content
    )
}
