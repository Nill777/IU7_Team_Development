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
    private companion object {
        const val DEFAULT_THRESHOLD = 5.0f
        const val ERROR_ANOMALY_SCORE = 999f
        const val ZERO_VAL = 0f
        const val ENSEMBLE_DIVISOR = 2.0
        const val SINGLE_RESULT_SIZE = 1
    }

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
        val activeModels = getModelsByStrategy()

        // прогоняем данные через активные модели
        val individualResults = activeModels.mapNotNull { model ->
            val specificModelProfile = profile.modelProfiles[model.modelName]
            val threshold = thresholds[model.modelName] ?: DEFAULT_THRESHOLD
            specificModelProfile?.let { model.verify(attempt, it, threshold) }
        }
        return when {
            individualResults.isEmpty() -> listOf(
                VerificationResult(
                    modelName = "Unknown",
                    isOwner = false,
                    anomalyScore = ERROR_ANOMALY_SCORE,
                    confidence = ZERO_VAL,
                    thresholdUsed = ZERO_VAL
                )
            )
            individualResults.size == SINGLE_RESULT_SIZE -> individualResults
            else -> {
                val ensembleResult = calculateEnsemble(individualResults)
                individualResults + ensembleResult
            }
        }
    }

    private fun getModelsByStrategy(): List<IVerificationModel> = when (currentStrategy) {
        VerificationStrategy.ENSEMBLE -> allModels
        VerificationStrategy.TIMING_ONLY -> listOf(timingModel)
        VerificationStrategy.SPATIAL_ONLY -> listOf(spatialModel)
        VerificationStrategy.MOTION_ONLY -> listOf(motionModel)
    }

    private fun calculateEnsemble(results: List<VerificationResult>): VerificationResult {
        val averageAnomaly = results.map { it.anomalyScore }.average().toFloat()
        val averageConfidence = results.map { it.confidence }.average().toFloat()
        val positiveVotes = results.count { it.isOwner }
        // если больше половины моделей подтвердили, пускаем
        val ensembleIsOwner = positiveVotes >= results.size / ENSEMBLE_DIVISOR

        return VerificationResult(
            modelName = "Ensemble",
            isOwner = ensembleIsOwner,
            anomalyScore = averageAnomaly,
            confidence = averageConfidence,
            thresholdUsed = ZERO_VAL
        )
    }
}
