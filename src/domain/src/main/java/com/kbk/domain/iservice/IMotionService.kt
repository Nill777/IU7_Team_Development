package com.kbk.domain.iservice

import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.MotionData
import kotlinx.coroutines.flow.StateFlow

interface IMotionService {
    val motionState: StateFlow<MotionData>
    fun start()
    fun stop()
    fun processSample(sample: BiometricSample)
}
