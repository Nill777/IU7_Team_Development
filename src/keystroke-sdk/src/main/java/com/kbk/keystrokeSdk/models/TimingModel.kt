package com.kbk.keystrokeSdk.models

import com.kbk.domain.models.BiometricSample

/**
 * Временная модель
 * Анализирует тайминги нажатий
 */
class TimingModel : BaseMahalanobisModel() {
    override val modelName: String = "TimingModel"

    override fun extractFeatures(sample: BiometricSample): DoubleArray {
        return doubleArrayOf(
            sample.touchData.dwellTime.toDouble(),
            sample.touchData.flightTime.toDouble()
        )
    }
}
