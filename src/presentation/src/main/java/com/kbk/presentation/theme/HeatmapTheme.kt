package com.kbk.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val HeatmapKeyboardColors = lightColorScheme(
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color.White,
    onSurface = Color.Black
)

@Composable
fun HeatmapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HeatmapKeyboardColors,
        content = content
    )
}
