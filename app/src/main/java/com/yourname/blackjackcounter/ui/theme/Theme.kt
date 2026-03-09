package com.yourname.blackjackcounter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = TableEdge,
    secondary = ChipGreen,
    onSecondary = TextLight,
    tertiary = ChipGreen,
    onTertiary = TextLight,
    error = ChipRed,
    onError = TextLight,
    background = TableEdge,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    onSurfaceVariant = MutedText
)

private val LightColorScheme = lightColorScheme(
    primary = FeltGreen,
    onPrimary = TextLight,
    secondary = ChipGreen,
    onSecondary = TextLight,
    tertiary = ChipGreen,
    onTertiary = TextLight,
    error = ChipRed,
    onError = TextLight,
    background = TextLight,
    onBackground = Slate,
    surface = Color.White,
    onSurface = Slate,
    onSurfaceVariant = Slate
)

@Composable
fun BlackjackCounterTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
