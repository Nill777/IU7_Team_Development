package com.kbk.domain.models.sdk

/**
 * Объект, представляющий одну попытку фоновой верификации и ее результаты
 */
data class VerificationAttempt(
    val timestamp: Long,
    val results: List<VerificationResult>
)
