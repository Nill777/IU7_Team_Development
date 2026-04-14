package com.kbk.domain.irepository

import com.kbk.domain.models.BiometricSample
import kotlinx.coroutines.flow.Flow

interface IBiometricRepository {
    suspend fun saveSample(sample: BiometricSample)
    fun getAllSamples(): Flow<List<BiometricSample>>
}
