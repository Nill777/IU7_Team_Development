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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private fun isRussian(k: String) = k.length == 1 && k.all { it in 'а'..'я' || it == 'ё' }
private fun isEnglish(k: String) = k.length == 1 && k.all { it in 'a'..'z' }

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

        const val DEFAULT_TIMING_THRESHOLD = 4.0f
        const val DEFAULT_SPATIAL_THRESHOLD = 5.0f
        const val DEFAULT_MOTION_THRESHOLD = 6.0f

    }

    private var globalThresholds = mapOf(
        "TimingModel" to DEFAULT_TIMING_THRESHOLD,
        "SpatialModel" to DEFAULT_SPATIAL_THRESHOLD,
        "MotionModel" to DEFAULT_MOTION_THRESHOLD
    )

    private val _verificationResultFlow = MutableStateFlow<VerificationResult?>(null)
    override val verificationResultFlow: StateFlow<VerificationResult?> =
        _verificationResultFlow.asStateFlow()

    private val _isVerificationMode = MutableStateFlow(false)
    override val isVerificationMode: StateFlow<Boolean> = _isVerificationMode.asStateFlow()

    private val _totalSamplesCount = MutableStateFlow(0)
    override val totalSamplesCount: StateFlow<Int> = _totalSamplesCount.asStateFlow()

    private val _trainedSamplesCount = MutableStateFlow(0)
    override val trainedSamplesCount: StateFlow<Int> = _trainedSamplesCount.asStateFlow()

    private val _playgroundSampleFlow =
        MutableSharedFlow<BiometricSample>(extraBufferCapacity = 100)
    override val playgroundSampleFlow: SharedFlow<BiometricSample> =
        _playgroundSampleFlow.asSharedFlow()

    // эталонный профиль владельца
    private var currentProfile: BiometricProfile? = null

    // буфер текущей попытки ввода
    private val attemptBuffer = mutableListOf<BiometricSample>()

    // счетчик успешно верифицированных и сохраненных записей с момента последнего обучения
    private var samplesAddedSinceLastTrain = 0
    private var isTestInputMode = false

    init {
        CoroutineScope(Dispatchers.IO).launch {
            _totalSamplesCount.value = biometricRepository.getSamplesCount()
        }
    }

    override fun updateGlobalThresholds(thresholds: Map<String, Float>) {
        globalThresholds = thresholds
    }

    override fun setTestInputMode(isActive: Boolean) {
        isTestInputMode = isActive
    }

    override fun startBiometricCollection() = motionRepository.startTracking()
    override fun stopBiometricCollection() = motionRepository.stopTracking()

    override suspend fun saveSample(touch: TouchData) {
        val currentMotion = motionRepository.motionState.value
        val sample = BiometricSample(
            touchData = touch,
            motionData = currentMotion
        )

        // в Playground, чтобы мог отрисовать
        _playgroundSampleFlow.tryEmit(sample)

        // тестовый ввод не сохраненяем в БД
        if (isTestInputMode) {
            return
        }
        _totalSamplesCount.value += 1

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

        val results = runCatching {
            verificationManager.verify(attemptBuffer, currentProfile!!, globalThresholds)
        }.getOrNull()

        val ensembleResult =
            results?.find { it.modelName.startsWith("Ensemble") } ?: results?.firstOrNull()
        _verificationResultFlow.value = ensembleResult

        if (ensembleResult?.isOwner == true) {
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

    override suspend fun trainProfileFromDb() {
        val allSamples = biometricRepository.getAllSamples().first()

        // применяем скользящее окно, берем только TRAINING_WINDOW_SIZE последних записей на каждую клавишу
        val trainingSamples = applySlidingWindow(allSamples, TRAINING_WINDOW_SIZE)

        currentProfile = verificationManager.train(trainingSamples)
        _trainedSamplesCount.value = trainingSamples.size
        _isVerificationMode.value = true
    }

    override fun verifyForPlayground(
        attempt: List<BiometricSample>,
        thresholds: Map<String, Float>
    ): List<VerificationResult>? {
        val profile = currentProfile ?: return null
        return runCatching {
            verificationManager.verify(attempt, profile, thresholds)
        }.getOrNull()
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
}
