package com.kbk.domain.irepository

import com.kbk.domain.models.BiometricSample

interface IBiometricRepository {
    suspend fun saveSample(sample: BiometricSample)
}
