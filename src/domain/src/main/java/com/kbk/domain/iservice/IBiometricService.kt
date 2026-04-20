package com.kbk.domain.iservice

import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.TouchData
import kotlinx.coroutines.flow.Flow

interface IBiometricService {
    fun startBiometricCollection()
    fun stopBiometricCollection()
    suspend fun saveSample(touch: TouchData)
    fun getCollectedSamples(): Flow<List<BiometricSample>>
    fun calculateTransitionMatrices(data: List<BiometricSample>):
            Pair<Map<Pair<String, String>, Float>, Map<Pair<String, String>, Float>>
}
