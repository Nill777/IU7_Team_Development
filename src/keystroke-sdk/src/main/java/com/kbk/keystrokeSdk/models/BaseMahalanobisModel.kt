package com.kbk.keystrokeSdk.models

import com.kbk.domain.isdk.IVerificationModel
import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.sdk.MahalanobisStats
import com.kbk.domain.models.sdk.ModelProfile
import com.kbk.domain.models.sdk.VerificationResult
import com.kbk.keystrokeSdk.math.MatrixMath

/**
 * Базовый класс для моделей, вычисляющих аномалии через расстояние Махаланобиса
 */
abstract class BaseMahalanobisModel : IVerificationModel {

    companion object {
        private const val MIN_SAMPLES_REQUIRED = 10
        private const val MIN_KEY_SAMPLES = 3
        private const val DEFAULT_CONFIDENCE = 1.0f
        private const val ZERO_CONFIDENCE = 0.0f
        private const val ZERO_SCORE = 0.0f
    }

    /**
     * Каждая модель-наследник извлекает только свой массив метрик.
     */
    protected abstract fun extractFeatures(sample: BiometricSample): DoubleArray

    override fun train(samples: List<BiometricSample>): ModelProfile {
        require(samples.size >= MIN_SAMPLES_REQUIRED) {
            "[$modelName] Недостаточно данных. Ожидается: $MIN_SAMPLES_REQUIRED, получено: ${samples.size}."
        }

        val groupedByKey = samples.groupBy { it.touchData.key }
        val statsMap = mutableMapOf<String, MahalanobisStats>()

        for ((key, keySamples) in groupedByKey) {
            // Пропускаем клавиши, по которым слишком мало данных для построения ковариации
            if (keySamples.size < MIN_KEY_SAMPLES) continue

            val vectors = keySamples.map { extractFeatures(it) }
            val mean = MatrixMath.mean(vectors)
            val cov = MatrixMath.covariance(vectors, mean)
            val invCov = MatrixMath.invert(cov)

            statsMap[key] = MahalanobisStats(mean, invCov)
        }

        return ModelProfile(modelName = this.modelName, keyStats = statsMap)
    }

    override fun verify(
        attempt: List<BiometricSample>,
        profile: ModelProfile,
        threshold: Float
    ): VerificationResult {
        var totalDistance = 0.0
        var evaluatedKeysCount = 0

        for (sample in attempt) {
            val stats = profile.keyStats[sample.touchData.key] ?: continue
            val features = extractFeatures(sample)

            val distance =
                MatrixMath.mahalanobis(features, stats.meanVector, stats.inverseCovariance)
            totalDistance += distance
            evaluatedKeysCount++
        }

        // Если пользователь вводил только те символы, которых нет в профиле
        if (evaluatedKeysCount == 0) {
            return VerificationResult(
                modelName = modelName,
                isOwner = true,
                anomalyScore = ZERO_SCORE,
                confidence = ZERO_CONFIDENCE,
                thresholdUsed = threshold
            )
        }

        val avgDistance = (totalDistance / evaluatedKeysCount).toFloat()
        val isOwner = avgDistance < threshold

        return VerificationResult(
            modelName = modelName,
            isOwner = isOwner,
            anomalyScore = avgDistance,
            confidence = DEFAULT_CONFIDENCE,
            thresholdUsed = threshold
        )
    }
}
