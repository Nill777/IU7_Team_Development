package com.kbk.domain.iservice

import com.kbk.domain.isdk.VerificationStrategy
import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.TouchData
import com.kbk.domain.models.sdk.VerificationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IBiometricService {
    // поток результатов верификации
    val verificationResultFlow: StateFlow<VerificationResult?>
    val isVerificationMode: StateFlow<Boolean>

    fun startBiometricCollection()
    fun stopBiometricCollection()
    suspend fun saveSample(touch: TouchData)
    fun getCollectedSamples(): Flow<List<BiometricSample>>
    fun calculateTransitionMatrices(data: List<BiometricSample>):
            Pair<Map<Pair<String, String>, Float>, Map<Pair<String, String>, Float>>
    suspend fun trainProfileFromDb()
    fun setVerificationStrategy(strategy: VerificationStrategy)
}
