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

    override fun verify(
        attempt: List<BiometricSample>,
        profile: BiometricProfile,
        thresholds: Map<String, Float>
    ): List<VerificationResult> {
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
            val threshold = thresholds[model.modelName] ?: 5.0f
            if (specificModelProfile != null) {
                model.verify(attempt, specificModelProfile, threshold)
            } else null
        }

        if (individualResults.isEmpty()) {
            return listOf(VerificationResult("Unknown", false, 999f, 0f, 0f))
        }

        if (individualResults.size == 1) return individualResults

        val averageAnomaly = individualResults.map { it.anomalyScore }.average().toFloat()
        val averageConfidence = individualResults.map { it.confidence }.average().toFloat()
        val positiveVotes = individualResults.count { it.isOwner }
        // если больше половины моделей подтвердили, пускаем
        val ensembleIsOwner = positiveVotes >= (individualResults.size / 2.0)

        val ensembleResult = VerificationResult(
            modelName = "Ensemble",
            isOwner = ensembleIsOwner,
            anomalyScore = averageAnomaly,
            confidence = averageConfidence,
            thresholdUsed = 0f // у ансамбля нет единого порога
        )

        return individualResults + ensembleResult
    }
}
