package com.kbk.domain.service

import com.kbk.domain.irepository.IBiometricRepository
import com.kbk.domain.irepository.IMotionRepository
import com.kbk.domain.iservice.IBiometricService
import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.TouchData
import kotlinx.coroutines.flow.Flow

class BiometricService(
    private val biometricRepository: IBiometricRepository,
    private val motionRepository: IMotionRepository
) : IBiometricService {
    private companion object {
        const val MAX_TRANSITION_TIME_MS = 2000L
    }

    override fun startBiometricCollection() = motionRepository.startTracking()
    override fun stopBiometricCollection() = motionRepository.stopTracking()

    override suspend fun saveSample(touch: TouchData) {
        val currentMotion = motionRepository.motionState.value
        val sample = BiometricSample(
            touchData = touch,
            motionData = currentMotion
        )
        biometricRepository.saveSample(sample)
    }

    override fun getCollectedSamples(): Flow<List<BiometricSample>> {
        return biometricRepository.getAllSamples()
    }

    override fun calculateTransitionMatrices(
        data: List<BiometricSample>
    ): Pair<Map<Pair<String, String>, Float>, Map<Pair<String, String>, Float>> {
        val ruRawMatrix = mutableMapOf<Pair<String, String>, MutableList<Long>>()
        val enRawMatrix = mutableMapOf<Pair<String, String>, MutableList<Long>>()
        val sortedData = data.sortedBy { it.motionData.timestamp }

        for (i in 0 until sortedData.size - 1) {
            val from = sortedData[i]
            val to = sortedData[i + 1]
            val flightTime = to.touchData.flightTime

            // игнорируем переходы дольше 2000 мс
            if (to.motionData.timestamp - from.motionData.timestamp < MAX_TRANSITION_TIME_MS &&
                flightTime < MAX_TRANSITION_TIME_MS
            ) {
                val fromKey = from.touchData.key.lowercase()
                val toKey = to.touchData.key.lowercase()

                if (isRussian(fromKey) && isRussian(toKey)) {
                    ruRawMatrix.getOrPut(fromKey to toKey) { mutableListOf() }.add(flightTime)
                } else if (isEnglish(fromKey) && isEnglish(toKey)) {
                    enRawMatrix.getOrPut(fromKey to toKey) { mutableListOf() }.add(flightTime)
                }
            }
        }
        return Pair(
            ruRawMatrix.mapValues { it.value.average().toFloat() },
            enRawMatrix.mapValues { it.value.average().toFloat() }
        )
    }

    private fun isRussian(k: String) = k.length == 1 && k.all { it in 'а'..'я' || it == 'ё' }
    private fun isEnglish(k: String) = k.length == 1 && k.all { it in 'a'..'z' }
}
