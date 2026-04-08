package com.kbk.presentation.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val SENSITIVITY_THRESHOLD = 2.5f
class SettingsViewModel : ViewModel() {
    private val _threshold = MutableStateFlow(SENSITIVITY_THRESHOLD)
    val threshold = _threshold.asStateFlow()

    fun updateThreshold(value: Float) {
        _threshold.value = value
    }
}
