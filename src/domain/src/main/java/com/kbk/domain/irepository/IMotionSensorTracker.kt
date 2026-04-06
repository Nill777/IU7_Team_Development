package com.kbk.domain.irepository

import com.kbk.domain.models.MotionData
import kotlinx.coroutines.flow.StateFlow

interface IMotionSensorTracker {
    val motionState: StateFlow<MotionData>

    fun startTracking()
    fun stopTracking()
}
