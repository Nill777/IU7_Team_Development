package com.kbk.domain.service

import com.kbk.domain.irepository.IBiometricRepository
import com.kbk.domain.iservice.IBiometricService
import com.kbk.domain.models.BiometricSample

class BiometricService(
    private val repository: IBiometricRepository
) : IBiometricService {
    override suspend fun processAndSaveSample(sample: BiometricSample) {
        repository.saveSample(sample)
    }
}
