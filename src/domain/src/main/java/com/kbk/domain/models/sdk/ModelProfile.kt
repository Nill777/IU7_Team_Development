package com.kbk.domain.models.sdk

/**
 * Эталонный профиль, вычисленный одной конкретной моделью
 */
data class ModelProfile(
    val modelName: String,
    // Ключ - символ клавиши
    // Значение - матрица Махаланобиса для этой клавиши
    val keyStats: Map<String, MahalanobisStats> = emptyMap()
)
