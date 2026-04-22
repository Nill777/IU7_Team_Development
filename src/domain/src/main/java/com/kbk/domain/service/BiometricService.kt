package com.kbk.domain.service

import com.kbk.domain.irepository.IBiometricRepository
import com.kbk.domain.irepository.IMotionRepository
import com.kbk.domain.isdk.IKeystrokeVerificationManager
import com.kbk.domain.isdk.VerificationStrategy
import com.kbk.domain.iservice.IBiometricService
import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.TouchData
import com.kbk.domain.models.sdk.BiometricProfile
import com.kbk.domain.models.sdk.VerificationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class BiometricService(
    private val biometricRepository: IBiometricRepository,
    private val motionRepository: IMotionRepository,
    private val verificationManager: IKeystrokeVerificationManager
) : IBiometricService {
    private companion object {
        const val MAX_TRANSITION_TIME_MS = 2000L

        // размер батча для проверки
        const val ATTEMPT_WINDOW_SIZE = 4

        // объем окна для обучения
        const val TRAINING_WINDOW_SIZE = 100

        // порог необходимого объема данных для верификации
        const val AUTO_TRAIN_THRESHOLD = 500

        // порог объема новых данных для переобучения
        const val EVOLUTION_THRESHOLD = 25
    }

    private val _verificationResultFlow = MutableStateFlow<VerificationResult?>(null)
    override val verificationResultFlow: StateFlow<VerificationResult?> =
        _verificationResultFlow.asStateFlow()

    // эталонный профиль владельца
    private var currentProfile: BiometricProfile? = null

    // буфер текущей попытки ввода
    private val attemptBuffer = mutableListOf<BiometricSample>()

    // счетчик успешно верифицированных и сохраненных записей с момента последнего обучения
    private var samplesAddedSinceLastTrain = 0

    override fun startBiometricCollection() = motionRepository.startTracking()
    override fun stopBiometricCollection() = motionRepository.stopTracking()

    override suspend fun saveSample(touch: TouchData) {
        val currentMotion = motionRepository.motionState.value
        val sample = BiometricSample(
            touchData = touch,
            motionData = currentMotion
        )

        // режим сбора данных
        if (currentProfile == null) {
            biometricRepository.saveSample(sample)
            checkAutoTrainTrigger()
        } else {
            // рабочий режим: верификация, защита от отравления, эволюция профиля
            processVerificationStep(sample)
        }
    }

    private suspend fun processVerificationStep(sample: BiometricSample) {
        attemptBuffer.add(sample)
        if (attemptBuffer.size < ATTEMPT_WINDOW_SIZE) return
        val result = verifyCurrentBuffer()
        if (result?.isOwner == true) {
            handleSuccessfulVerification()
        }

        attemptBuffer.clear()
    }

    private suspend fun handleSuccessfulVerification() {
        // сохраняем проверенные данные в БД
        attemptBuffer.forEach { biometricRepository.saveSample(it) }
        samplesAddedSinceLastTrain += attemptBuffer.size

        // эволюция профиля
        if (samplesAddedSinceLastTrain >= EVOLUTION_THRESHOLD) {
            trainProfileFromDb()
            samplesAddedSinceLastTrain = 0
        }
    }

    private suspend fun checkAutoTrainTrigger() {
        val count = biometricRepository.getSamplesCount()

        if (count >= AUTO_TRAIN_THRESHOLD) {
            runCatching { trainProfileFromDb() }
        }
    }


    private fun verifyCurrentBuffer(): VerificationResult? {
        val profile = currentProfile ?: return null
        val result = runCatching {
            verificationManager.verify(attemptBuffer, profile)
        }.getOrNull()

        _verificationResultFlow.value = result
        return result
    }

    override suspend fun trainProfileFromDb() {
        val allSamples = biometricRepository.getAllSamples().first()

        // применяем скользящее окно, берем только TRAINING_WINDOW_SIZE последних записей на каждую клавишу
        val trainingSamples = applySlidingWindow(allSamples, TRAINING_WINDOW_SIZE)

        currentProfile = verificationManager.train(trainingSamples)
    }

    private fun applySlidingWindow(
        samples: List<BiometricSample>,
        windowSize: Int
    ): List<BiometricSample> {
        return samples.groupBy { it.touchData.key }
            .flatMap { (_, samplesForKey) ->
                // сортируем от новых к старым и берем окно
                samplesForKey
                    .sortedByDescending { it.motionData.timestamp }
                    .take(windowSize)
            }
    }

    override fun setVerificationStrategy(strategy: VerificationStrategy) {
        verificationManager.setStrategy(strategy)
    }

    override fun getCollectedSamples(): Flow<List<BiometricSample>> =
        biometricRepository.getAllSamples()

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
