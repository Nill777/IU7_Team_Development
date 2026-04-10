package com.kbk.domain.iservice

import com.kbk.domain.models.BiometricSample

interface IBiometricService {
    suspend fun processAndSaveSample(sample: BiometricSample)
}
