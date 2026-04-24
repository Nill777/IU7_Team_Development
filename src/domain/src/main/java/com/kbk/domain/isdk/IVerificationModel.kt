package com.kbk.domain.isdk

import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.sdk.ModelProfile
import com.kbk.domain.models.sdk.VerificationResult

/**
 * Контракт для любой математической модели
 */
interface IVerificationModel {
    val modelName: String

    /**
     * Обучает модель и строит ее персональный эталонный профиль
     * @throws IllegalArgumentException если передано недостаточно данных для невырожденной матрицы
     */
    @Throws(IllegalArgumentException::class)
    fun train(samples: List<BiometricSample>): ModelProfile

    fun verify(
        attempt: List<BiometricSample>,
        profile: ModelProfile,
        threshold: Float
    ): VerificationResult
}
