package com.kbk.keystrokeSdk.models

import com.kbk.domain.models.BiometricSample

/**
 * Пространственная модель
 * Анализирует координаты точки касания и смещение пальца во время удержания.
 */
class SpatialModel : BaseMahalanobisModel() {
    override val modelName: String = "SpatialModel"

    override fun extractFeatures(sample: BiometricSample): DoubleArray {
        return doubleArrayOf(
            sample.touchData.touchX.toDouble(),
            sample.touchData.touchY.toDouble(),
            sample.touchData.swipeVectorX.toDouble(),
            sample.touchData.swipeVectorY.toDouble()
        )
    }
}
