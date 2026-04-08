package com.kbk.presentation.playground

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class PlaygroundViewModel : ViewModel() {
    val attempts = MutableStateFlow<List<String>>(emptyList())

    fun addAttempt(isOwner: Boolean) {
        val status = if (isOwner) "Владелец" else "Злоумышленник"
        attempts.value = attempts.value + status
    }
}
