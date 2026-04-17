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

    init {
        biometricService.getCollectedSamples()
            .onEach { data ->
                // cортировка клавиш по убыванию количества записей (популярные сверху)
                val keysFreq = data.groupingBy { it.touchData.key }.eachCount()
                val sortedKeys = keysFreq.entries.sortedByDescending { it.value }.map { it.key }

                // матрицы переходов по flight
                val ruRawMatrix = mutableMapOf<Pair<String, String>, MutableList<Long>>()
                val enRawMatrix = mutableMapOf<Pair<String, String>, MutableList<Long>>()

                val sortedData = data.sortedBy { it.motionData.timestamp }

                for (i in 0 until sortedData.size - 1) {
                    val from = sortedData[i]
                    val to = sortedData[i + 1]
                    val flightTime = to.touchData.flightTime

                    // игнорируем переходы дольше 2000 мс
                    if (to.motionData.timestamp - from.motionData.timestamp < 2000L && flightTime < 2000L) {
                        val fromKey = from.touchData.key.lowercase()
                        val toKey = to.touchData.key.lowercase()

                        val isRu = fromKey.length == 1 && toKey.length == 1 &&
                                fromKey.all { it in 'а'..'я' || it == 'ё' } &&
                                toKey.all { it in 'а'..'я' || it == 'ё' }

                        val isEn = fromKey.length == 1 && toKey.length == 1 &&
                                fromKey.all { it in 'a'..'z' } &&
                                toKey.all { it in 'a'..'z' }

                        if (isRu) {
                            ruRawMatrix.getOrPut(fromKey to toKey) { mutableListOf() }
                                .add(flightTime)
                        } else if (isEn) {
                            enRawMatrix.getOrPut(fromKey to toKey) { mutableListOf() }
                                .add(flightTime)
                        }
                    }
                }

                val ruMatrix = ruRawMatrix.mapValues { it.value.average().toFloat() }
                val enMatrix = enRawMatrix.mapValues { it.value.average().toFloat() }

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
