package com.wifiscanner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF66C2FF),
    secondary = Color(0xFF4DD0E1),
    tertiary = Color(0xFF9CE6B5),
    background = Color(0xFF08131D),
    surface = Color(0xFF112332),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFEAF2FF),
    onSurface = Color(0xFFEAF2FF)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E88E5),
    secondary = Color(0xFF00ACC1),
    tertiary = Color(0xFF43A047),
    background = Color(0xFFF5F9FF),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1A1C20),
    onSurface = Color(0xFF1A1C20)
)

@Composable
fun WifiScannerTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
