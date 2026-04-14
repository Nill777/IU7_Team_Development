package com.kbk.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kbk.domain.iservice.IBiometricService
import com.kbk.domain.models.BiometricSample
import com.kbk.presentation.keyboard.KeyboardLayouts
import kotlinx.coroutines.flow.*

enum class HeatmapMetricType(val label: String, val unit: String) {
    FREQUENCY("Частота нажатий", "шт"),
    DWELL_TIME("Время удержания", "мс"),
    PRESSURE("Сила нажатия", "у.е.")
}

data class DashboardUiState(
    val samples: List<BiometricSample> = emptyList(),
    val isLoading: Boolean = true,
    val heatmapMetric: HeatmapMetricType = HeatmapMetricType.FREQUENCY,
    val sortedAvailableKeys: List<String> = emptyList(),
    val selectedKey: String = "",
    val transitionMatrix: Map<Pair<String, String>, Float> = emptyMap()
)

class DashboardViewModel(
    private val biometricService: IBiometricService
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        biometricService.getCollectedSamples()
            .onEach { data ->
                // cортировка клавиш по убыванию количества записей (популярные сверху)
                val keysFreq = data.groupingBy { it.touchData.key }.eachCount()
                val sortedKeys = keysFreq.entries.sortedByDescending { it.value }.map { it.key }

                // Вычисление матрицы переходов (Flight Time)
                val matrix = mutableMapOf<Pair<String, String>, Float>()
                val sortedData = data.sortedBy { it.motionData.timestamp }
                for (i in 0 until sortedData.size - 1) {
                    val from = sortedData[i]
                    val to = sortedData[i + 1]
                    if (to.motionData.timestamp - from.motionData.timestamp < 2000L) {
                        val pair = from.touchData.key to to.touchData.key
                        matrix[pair] = to.touchData.flightTime.toFloat()
                    }
                }

                _uiState.value = _uiState.value.copy(
                    samples = data,
                    isLoading = false,
                    sortedAvailableKeys = sortedKeys,
                    selectedKey = sortedKeys.firstOrNull() ?: "а",
                    transitionMatrix = matrix
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
