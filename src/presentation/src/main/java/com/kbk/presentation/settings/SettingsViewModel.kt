package com.kbk.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kbk.domain.irepository.ISettingsRepository
import com.kbk.domain.iservice.IBiometricService
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: ISettingsRepository,
    biometricService: IBiometricService
) : ViewModel() {

    val batchSize = settingsRepository.batchSize
    val timingThreshold = settingsRepository.timingThreshold
    val spatialThreshold = settingsRepository.spatialThreshold
    val motionThreshold = settingsRepository.motionThreshold

    // лента истории из самого сервиса
    val verificationHistory = biometricService.verificationHistoryFlow

    fun updateBatchSize(size: Float) {
        viewModelScope.launch { settingsRepository.setBatchSize(size.toInt()) }
    }

    fun updateTimingThreshold(value: Float) {
        viewModelScope.launch { settingsRepository.setTimingThreshold(value) }
    }

    fun updateSpatialThreshold(value: Float) {
        viewModelScope.launch { settingsRepository.setSpatialThreshold(value) }
    }

    fun updateMotionThreshold(value: Float) {
        viewModelScope.launch { settingsRepository.setMotionThreshold(value) }
    }
}
