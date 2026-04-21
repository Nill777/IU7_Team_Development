package com.kbk.domain.models.sdk

/**
 * Результат оценки попытки ввода конкретной моделью или всем ансамблем
 */
data class VerificationResult(
    val modelName: String,
    val isOwner: Boolean,
    val anomalyScore: Float, // среднее расстояние Махаланобиса
    val confidence: Float    // степень уверенности алгоритма
)
