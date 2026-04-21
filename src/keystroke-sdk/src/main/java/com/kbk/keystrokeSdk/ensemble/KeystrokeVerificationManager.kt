package com.kbk.keystrokeSdk.ensemble

import com.kbk.domain.isdk.IKeystrokeVerificationManager
import com.kbk.domain.isdk.IVerificationModel
import com.kbk.domain.isdk.VerificationStrategy
import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.sdk.BiometricProfile
import com.kbk.domain.models.sdk.VerificationResult
import com.kbk.keystrokeSdk.models.MotionModel
import com.kbk.keystrokeSdk.models.SpatialModel
import com.kbk.keystrokeSdk.models.TimingModel

/**
 * Управляет всеми моделями, решает, кому делегировать проверку, и проводит голосование
 */
class KeystrokeVerificationManager(
    private val timingModel: IVerificationModel = TimingModel(),
    private val spatialModel: IVerificationModel = SpatialModel(),
    private val motionModel: IVerificationModel = MotionModel()
) : IKeystrokeVerificationManager {

    private var currentStrategy: VerificationStrategy = VerificationStrategy.ENSEMBLE
    private val allModels = listOf(timingModel, spatialModel, motionModel)

    override fun setStrategy(strategy: VerificationStrategy) {
        this.currentStrategy = strategy
    }

    override fun train(samples: List<BiometricSample>): BiometricProfile {
        // всегда обучаем все 3 модели
        val profilesMap = allModels.associate { model ->
            model.modelName to model.train(samples)
        }

        return BiometricProfile(modelProfiles = profilesMap)
    }

    override fun verify(attempt: List<BiometricSample>, profile: BiometricProfile): VerificationResult {
        // паттерн Strategy
        val activeModels = when (currentStrategy) {
            VerificationStrategy.ENSEMBLE -> allModels
            VerificationStrategy.TIMING_ONLY -> listOf(timingModel)
            VerificationStrategy.SPATIAL_ONLY -> listOf(spatialModel)
            VerificationStrategy.MOTION_ONLY -> listOf(motionModel)
        }

        // прогоняем данные через активные модели
        val individualResults = activeModels.mapNotNull { model ->
            val specificModelProfile = profile.modelProfiles[model.modelName]
            if (specificModelProfile != null) {
                model.verify(attempt, specificModelProfile)
            } else {
                null
            }
        }

        if (individualResults.isEmpty()) {
            return VerificationResult("Unknown", isOwner = false, anomalyScore = 999f, confidence = 0f)
        }

        // если стратегия - одиночная модель, возвращаем её результат
        if (individualResults.size == 1) {
            return individualResults.first()
        }

        // паттерн ensemble
        // насколько в среднем все модели отклонились
        val averageAnomaly = individualResults.map { it.anomalyScore }.average().toFloat()

        // средняя уверенность моделей
        val averageConfidence = individualResults.map { it.confidence }.average().toFloat()

        // Считаем голоса. Сколько моделей сказало "Да, это хозяин"?
        val positiveVotes = individualResults.count { it.isOwner }

        // если больше половины моделей подтвердили, пускаем
        val ensembleIsOwner = positiveVotes >= (individualResults.size / 2.0)

        return VerificationResult(
            modelName = "Ensemble (${individualResults.size} models)",
            isOwner = ensembleIsOwner,
            anomalyScore = averageAnomaly,
            confidence = averageConfidence
        )
    }
}
