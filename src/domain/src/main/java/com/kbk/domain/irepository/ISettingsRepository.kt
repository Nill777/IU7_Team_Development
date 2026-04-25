package com.kbk.domain.irepository

import kotlinx.coroutines.flow.StateFlow

interface ISettingsRepository {
    val batchSize: StateFlow<Int>
    val timingThreshold: StateFlow<Float>
    val spatialThreshold: StateFlow<Float>
    val motionThreshold: StateFlow<Float>

    suspend fun setBatchSize(size: Int)
    suspend fun setTimingThreshold(value: Float)
    suspend fun setSpatialThreshold(value: Float)
    suspend fun setMotionThreshold(value: Float)
}
