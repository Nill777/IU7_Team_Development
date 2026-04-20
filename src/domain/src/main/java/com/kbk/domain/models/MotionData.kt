package com.kbk.domain.models

data class MotionData(
    val timestamp: Long = 0L,
    val accX: Float = 0f,
    val accY: Float = 0f,
    val accZ: Float = 0f,
    val gyroX: Float = 0f,
    val gyroY: Float = 0f,
    val gyroZ: Float = 0f,
    val rotVecX: Float = 0f,
    val rotVecY: Float = 0f,
    val rotVecZ: Float = 0f,
    val gravX: Float = 0f,
    val gravY: Float = 0f,
    val gravZ: Float = 0f

)
