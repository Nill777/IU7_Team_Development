package com.kbk.keystrokeSdk.models

import com.kbk.domain.models.BiometricSample
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Микромоторная модель
 * Рассчитывает магнитуды векторов
 */
class MotionModel : BaseMahalanobisModel() {
    override val modelName: String = "MotionModel"

    private companion object {
        const val SQUARED_POWER = 2f
    }

    override fun extractFeatures(sample: BiometricSample): DoubleArray {
        val m = sample.motionData

        val accelMag =
            sqrt(m.accX.pow(SQUARED_POWER) + m.accY.pow(SQUARED_POWER) + m.accZ.pow(SQUARED_POWER))
        val gyroMag =
            sqrt(m.gyroX.pow(SQUARED_POWER) + m.gyroY.pow(SQUARED_POWER) + m.gyroZ.pow(SQUARED_POWER))
        val gravMag =
            sqrt(m.gravX.pow(SQUARED_POWER) + m.gravY.pow(SQUARED_POWER) + m.gravZ.pow(SQUARED_POWER))

        return doubleArrayOf(
            accelMag.toDouble(),
            gyroMag.toDouble(),
            gravMag.toDouble()
        )
    }
}
