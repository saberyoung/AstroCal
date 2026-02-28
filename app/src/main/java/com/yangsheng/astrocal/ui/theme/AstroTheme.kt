package com.yangsheng.astrocal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * AstroTheme: professional astronomy dark theme with auto day/night.
 * Keep it simple & stable.
 */

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7C8CFF),
    onPrimary = Color(0xFF0B1020),
    secondary = Color(0xFF8AD7FF),
    onSecondary = Color(0xFF06131A),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFE8EAFF),
    surface = Color(0xFF111A33),
    onSurface = Color(0xFFE8EAFF),
    surfaceVariant = Color(0xFF1A2446),
    onSurfaceVariant = Color(0xFFB9C2F3),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2A0B0B)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F49FF),
    onPrimary = Color.White,
    secondary = Color(0xFF006B8F),
    onSecondary = Color.White,
    background = Color(0xFFF6F7FF),
    onBackground = Color(0xFF0B1020),
    surface = Color.White,
    onSurface = Color(0xFF0B1020),
    surfaceVariant = Color(0xFFE7E9FF),
    onSurfaceVariant = Color(0xFF3A4168),
    error = Color(0xFFB00020),
    onError = Color.White
)

@Composable
fun AstroTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}