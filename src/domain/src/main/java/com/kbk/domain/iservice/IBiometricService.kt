package com.kbk.domain.iservice

import com.kbk.domain.isdk.VerificationStrategy
import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.TouchData
import com.kbk.domain.models.sdk.VerificationAttempt
import com.kbk.domain.models.sdk.VerificationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IBiometricService {
    // поток результатов верификации
    val verificationResultFlow: StateFlow<VerificationResult?>

    // история фоновых верификаций
    val verificationHistoryFlow: StateFlow<List<VerificationAttempt>>

    val isVerificationMode: StateFlow<Boolean>

    val totalSamplesCount: StateFlow<Int>
    val trainedSamplesCount: StateFlow<Int>

    val playgroundSampleFlow: SharedFlow<BiometricSample>

    fun startBiometricCollection()
    fun stopBiometricCollection()
    suspend fun saveSample(touch: TouchData)
    fun getCollectedSamples(): Flow<List<BiometricSample>>
    fun calculateTransitionMatrices(data: List<BiometricSample>):
            Pair<Map<Pair<String, String>, Float>, Map<Pair<String, String>, Float>>

    suspend fun trainProfileFromDb()
    fun setVerificationStrategy(strategy: VerificationStrategy)

    fun verifyForPlayground(
        attempt: List<BiometricSample>,
        thresholds: Map<String, Float>
    ): List<VerificationResult>?

    // блокировка записи в базу для тестовых данных
    fun setTestInputMode(isActive: Boolean)
}
