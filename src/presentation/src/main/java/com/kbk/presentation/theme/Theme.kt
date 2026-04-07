package com.kbk.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private const val DARK_GREY = 0xFF1E1E1E
private const val MEDIUM_GREY = 0xFF2D2D2D
private const val LIGHT_GREY = 0xFF424242
private const val OFF_WHITE = 0xFFECECEC
private const val SILVER = 0xFFD4D4D4

private val DarkColors = darkColorScheme(
    background = Color(DARK_GREY),
    surface = Color(MEDIUM_GREY),
    surfaceVariant = Color(LIGHT_GREY),
    onSurface = Color.White
)

private val LightColors = lightColorScheme(
    background = Color(OFF_WHITE),
    surface = Color.White,
    surfaceVariant = Color(SILVER),
    onSurface = Color.Black
)

@Composable
fun KeyboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
