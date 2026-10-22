package com.luna.cycle.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class LunaPalette(
    val accent: Color,
    val highlight: Color,
    val background: Color,
    val surface: Color,
    val foreground: Color
)

private val AccentOptions = listOf(
    Color(0xFFE88785), Color(0xFFDF7861), Color(0xFF9674C8),
    Color(0xFF578B72), Color(0xFF4F7FA8), Color(0xFF56515C)
)
val LocalLunaAccent = staticCompositionLocalOf { Color(0xFFE88785) }
val LocalLunaHighlight = staticCompositionLocalOf { Color(0xFFF3C5C5) }
val LocalLunaHeroStart = staticCompositionLocalOf { Color(0xFF482E52) }
val LocalLunaHeroEnd = staticCompositionLocalOf { Color(0xFFAD5E70) }

@Composable
fun LunaTheme(
    themeName: String = "Pastel",
    accentIndex: Int = 0,
    darkMode: Boolean? = null,
    content: @Composable () -> Unit
) {
    val dark = darkMode ?: isSystemInDarkTheme()
    val accent = AccentOptions.getOrElse(accentIndex) { AccentOptions.first() }
    val palette = when (themeName) {
        "Warm earth" -> LunaPalette(accent, Color(0xFFF1DDC6), Color(0xFFFAF5EE), Color(0xFFFFFCF8), Color(0xFF3E332B))
        "High contrast" -> LunaPalette(accent, Color(0xFFFFE36E), Color(0xFFF8F8F8), Color.White, Color.Black)
        "Monochrome" -> LunaPalette(Color(0xFF55515A), Color(0xFFE3E0E5), Color(0xFFF8F7F8), Color.White, Color(0xFF29272B))
        else -> LunaPalette(accent, Color(0xFFF3C5C5), Color(0xFFFAF8F7), Color.White, Color(0xFF342D30))
    }
    val heroStart = if (dark) Color(0xFF292133) else Color(0xFF49324F)
    val heroEnd = if (dark) Color(0xFF654253) else Color(0xFFB45F70)
    MaterialTheme(
        colorScheme = if (dark) {
            darkColorScheme(
                primary = accent,
                secondary = palette.highlight,
                tertiary = Color(0xFFE9B4A7),
                background = Color(0xFF17141B),
                surface = Color(0xFF25212B),
                surfaceVariant = Color(0xFF302A36),
                primaryContainer = Color(0xFF483343),
                secondaryContainer = Color(0xFF28362F),
                tertiaryContainer = Color(0xFF352C40),
                onBackground = Color(0xFFF6F0F1),
                onSurface = Color(0xFFF6F0F1),
                onSurfaceVariant = Color(0xFFC5BAC8),
                outline = Color(0xFF514957)
            )
        } else {
            lightColorScheme(
                primary = palette.accent,
                secondary = palette.highlight,
                tertiary = Color(0xFF8C6BA7),
                background = palette.background,
                surface = palette.surface,
                surfaceVariant = Color(0xFFF3EDF2),
                primaryContainer = Color(0xFFF5DFE1),
                secondaryContainer = Color(0xFFE8F1E8),
                tertiaryContainer = Color(0xFFF0EAF6),
                onBackground = palette.foreground,
                onSurface = palette.foreground,
                onSurfaceVariant = Color(0xFF786D78),
                outline = Color(0xFFD8CDD5)
            )
        },
        content = {
            CompositionLocalProvider(
                LocalLunaAccent provides palette.accent,
                LocalLunaHighlight provides palette.highlight,
                LocalLunaHeroStart provides heroStart,
                LocalLunaHeroEnd provides heroEnd,
                content = content
            )
        }
    )
}
