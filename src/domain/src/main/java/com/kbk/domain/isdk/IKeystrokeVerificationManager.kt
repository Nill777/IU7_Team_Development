package com.kbk.domain.isdk

import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.sdk.BiometricProfile
import com.kbk.domain.models.sdk.VerificationResult

/**
 * Менеджер нашего SDK
 */
interface IKeystrokeVerificationManager {

    /**
     * Позволяет переключать алгоритмы "на лету" без переобучения профиля
     */
    fun setStrategy(strategy: VerificationStrategy)

    /**
     * Прогоняет сырые данные через ВСЕ доступные модели и собирает единый BiometricProfile
     * @throws IllegalArgumentException если передано недостаточно данных.
     */
    @Throws(IllegalArgumentException::class)
    fun train(samples: List<BiometricSample>): BiometricProfile

    /**
     * Оценивает попытку на основе выбранной стратегии
     */
    fun verify(
        attempt: List<BiometricSample>,
        profile: BiometricProfile,
        thresholds: Map<String, Float>
    ): List<VerificationResult>
}
