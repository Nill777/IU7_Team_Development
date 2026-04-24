package com.kbk.presentation.playground

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kbk.domain.iservice.IBiometricService
import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.sdk.VerificationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaygroundViewModel(
    private val biometricService: IBiometricService
) : ViewModel() {
    val isVerificationMode = biometricService.isVerificationMode
    val totalSamplesCount = biometricService.totalSamplesCount
    val trainedSamplesCount = biometricService.trainedSamplesCount

    val batchSize = MutableStateFlow(4)
    val testText = MutableStateFlow("")

    private val _latestResults = MutableStateFlow<List<VerificationResult>>(emptyList())
    val latestResults = _latestResults.asStateFlow()

    private val localBuffer = mutableListOf<BiometricSample>()

    val timingThreshold = MutableStateFlow(4.0f)
    val spatialThreshold = MutableStateFlow(5.0f)
    val motionThreshold = MutableStateFlow(6.0f)

    suspend fun collectPlaygroundSamples() {
        biometricService.playgroundSampleFlow.collect { sample ->
            if (!isVerificationMode.value) return@collect
            if (testText.value.isEmpty()) return@collect

            localBuffer.add(sample)
            if (localBuffer.size >= batchSize.value) {

                val playgroundThresholds = mapOf(
                    "TimingModel" to timingThreshold.value,
                    "SpatialModel" to spatialThreshold.value,
                    "MotionModel" to motionThreshold.value
                )

                val results =
                    biometricService.verifyForPlayground(localBuffer, playgroundThresholds)
                if (results != null) {
                    _latestResults.value = results
                }
                localBuffer.clear()
            }
        }
    }

    // изоляция от БД в BiometricService
    fun setTestInputFocus(isFocused: Boolean) {
        biometricService.setTestInputMode(isFocused)
    }

    fun updateTimingThreshold(value: Float) {
        timingThreshold.value = value
    }

    fun updateSpatialThreshold(value: Float) {
        spatialThreshold.value = value
    }

    fun updateMotionThreshold(value: Float) {
        motionThreshold.value = value
    }

    fun updateTestText(text: String) {
        testText.value = text
        if (text.isEmpty()) {
            localBuffer.clear()
            _latestResults.value = emptyList()
        }
    }

    fun updateBatchSize(size: Float) {
        batchSize.value = size.toInt()
        localBuffer.clear()
    }

    fun trainModel() {
        viewModelScope.launch {
            try {
                biometricService.trainProfileFromDb()
            } catch (e: IllegalArgumentException) {
                Log.e("PlaygroundViewModel", "$e.message")
            } catch (e: IllegalStateException) {
                Log.e("PlaygroundViewModel", "ошибка математического ядра: $e.message")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // выключаем режим тестирования при закрытии экрана
        biometricService.setTestInputMode(false)
    }
}
