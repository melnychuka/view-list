package com.example.viewlist.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color(0xFF001F2A),
    primaryContainer = Color(0xFF003544),
    onPrimaryContainer = AccentCyan,
    secondary = AccentViolet,
    onSecondary = Color(0xFF1B0054),
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
