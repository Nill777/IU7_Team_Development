package com.kbk.domain.models.sdk

/**
 * Биометрический профиль владельца устройства
 * Хранит внутри себя знания от всех доступных математических моделей
 */
data class BiometricProfile(
    val ownerId: String = "local_user",
    // Ключ - имя модели
    // Значение - вычисленный профиль именно для этой модели
    val modelProfiles: Map<String, ModelProfile> = emptyMap()
)
