package com.kbk.domain.service

import com.kbk.domain.irepository.IBiometricRepository
import com.kbk.domain.models.BiometricSample

class BiometricService(
    private val repository: IBiometricRepository
) {
    suspend fun processAndSaveSample(sample: BiometricSample) {
        repository.saveSample(sample)
    }
}
