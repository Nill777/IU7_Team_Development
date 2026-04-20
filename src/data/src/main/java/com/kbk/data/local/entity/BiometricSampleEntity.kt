package com.kbk.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "biometric_samples",
    indices = [Index(value = ["key", "id"])]
)
data class BiometricSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val dwellTime: Long,
    val flightTime: Long,
    val pressure: Float,
    val touchX: Float,
    val touchY: Float,
    val swipeVectorX: Float,
    val swipeVectorY: Float,
    val timestamp: Long,
    val accX: Float,
    val accY: Float,
    val accZ: Float,
    val gyroX: Float,
    val gyroY: Float,
    val gyroZ: Float,
    val rotVecX: Float,
    val rotVecY: Float,
    val rotVecZ: Float,
    val gravX: Float,
    val gravY: Float,
    val gravZ: Float
)
