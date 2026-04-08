package com.kbk.presentation.playground

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun PlaygroundScreen(viewModel: PlaygroundViewModel) {
    val attempts by viewModel.attempts.collectAsState()

    LazyColumn {
        items(attempts) { text ->
            Text(text = "Результат: $text")
        }
    }
}
