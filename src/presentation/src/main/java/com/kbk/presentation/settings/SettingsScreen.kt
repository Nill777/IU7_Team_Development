package com.kbk.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val threshold by viewModel.threshold.collectAsState()

    Column(Modifier.padding(16.dp)) {
        Text("Порог чувствительности: ${"%.2f".format(threshold)}")
        Slider(
            value = threshold,
            onValueChange = { viewModel.updateThreshold(it) },
            valueRange = 1f..5f
        )
    }
}
