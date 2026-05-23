package com.example.viewlist.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = Color(0xFF0D0020),
    primaryContainer = Color(0xFF2D1B4A),
    onPrimaryContainer = AccentPrimary,
    secondary = AccentIndigo,
    onSecondary = Color(0xFF0A0820),
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = CardBg,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = DeleteRed,
)

@Composable
fun ViewListTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content,
    )
}
