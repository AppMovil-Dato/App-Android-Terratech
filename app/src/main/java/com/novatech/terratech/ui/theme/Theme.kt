package com.novatech.terratech.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors =
    lightColorScheme(
        primary = FarmGreen,
        onPrimary = Color.White,
        primaryContainer = LeafLight,
        onPrimaryContainer = Ink,
        secondary = FarmGreen,
        secondaryContainer = LeafLight,
        background = Mist,
        onBackground = Ink,
        surface = Color.White,
        onSurface = Ink,
        surfaceVariant = LeafLight,
        onSurfaceVariant = Muted,
        outline = Color(0xFFDCE5DF),
        outlineVariant = Color(0xFFDCE5DF),
        surfaceTint = FarmGreen,
        surfaceContainer = Color.White,
        error = Color(0xFFB3261E),
    )

@Composable
fun TerraTechTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = Typography, content = content)
}
