package com.vectorlogic.blackjackcounter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = GoldBright,
    onPrimary = TableEdge,
    secondary = FeltGreenBright,
    onSecondary = WarmText,
    tertiary = ChipGreen,
    onTertiary = TextLight,
    error = ChipRed,
    onError = TextLight,
    background = TableEdge,
    onBackground = TextLight,
    surface = SurfaceDark,
    surfaceVariant = SurfaceRaised,
    onSurface = TextLight,
    onSurfaceVariant = MutedText,
    secondaryContainer = SurfaceHighlight,
    onSecondaryContainer = WarmText,
    tertiaryContainer = FeltGreenSoft,
    onTertiaryContainer = WarmText,
    outline = DividerGreen,
    outlineVariant = TableEdgeSoft
)

private val LightColorScheme = lightColorScheme(
    primary = FeltGreen,
    onPrimary = TextLight,
    secondary = FeltGreenBright,
    onSecondary = TextLight,
    tertiary = ChipGreen,
    onTertiary = TextLight,
    error = ChipRed,
    onError = TextLight,
    background = TextLight,
    onBackground = Slate,
    surface = Color.White,
    surfaceVariant = Color(0xFFF4F1E7),
    onSurface = Slate,
    onSurfaceVariant = Color(0xFF53605A),
    secondaryContainer = Color(0xFFE7EFEA),
    onSecondaryContainer = Slate,
    tertiaryContainer = Color(0xFFE4F4E8),
    onTertiaryContainer = Slate,
    outline = Color(0xFFD0D6D1),
    outlineVariant = Color(0xFFE6E8E5)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(6.dp)
)

@Composable
fun BlackjackCounterTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
