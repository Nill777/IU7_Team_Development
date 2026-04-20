package com.kbk.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kbk.domain.iservice.IBiometricService
import com.kbk.domain.models.BiometricSample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

enum class HeatmapMetricType(val label: String, val unit: String) {
    FREQUENCY("Частота нажатий", "шт"),
    DWELL_TIME("Время удержания", "мс"),
    PRESSURE("Сила нажатия", "у.е.")
}

enum class SensorType {
    ACCELEROMETER, GYROSCOPE, ROTATION_VECTOR
}

data class DashboardUiState(
    val samples: List<BiometricSample> = emptyList(),
    val isLoading: Boolean = true,
    val heatmapMetric: HeatmapMetricType = HeatmapMetricType.FREQUENCY,
    val sortedAvailableKeys: List<String> = emptyList(),
    val selectedKey: String = "",
    val ruTransitionMatrix: Map<Pair<String, String>, Float> = emptyMap(),
    val enTransitionMatrix: Map<Pair<String, String>, Float> = emptyMap()
)

class DashboardViewModel(
    private val biometricService: IBiometricService
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private companion object {
        const val MAX_TRANSITION_TIME_MS = 2000L
    }

    init {
        biometricService.getCollectedSamples()
            .onEach { data ->
                // cортировка клавиш по убыванию количества записей (популярные сверху)
                val keysFreq = data.groupingBy { it.touchData.key }.eachCount()
                val sortedKeys = keysFreq.entries.sortedByDescending { it.value }.map { it.key }

                // матрицы переходов по flight
                val (ruMatrix, enMatrix) = biometricService.calculateTransitionMatrices(data)

                _uiState.value = _uiState.value.copy(
                    samples = data,
                    isLoading = false,
                    sortedAvailableKeys = sortedKeys,
                    selectedKey = sortedKeys.firstOrNull() ?: "а",
                    ruTransitionMatrix = ruMatrix,
                    enTransitionMatrix = enMatrix
                )
            }.launchIn(viewModelScope)
    }

    fun setHeatmapMetric(metric: HeatmapMetricType) {
        _uiState.value = _uiState.value.copy(heatmapMetric = metric)
    }

    fun setSelectedKey(key: String) {
        _uiState.value = _uiState.value.copy(selectedKey = key)
    }
}
