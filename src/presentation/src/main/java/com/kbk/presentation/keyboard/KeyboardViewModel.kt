package com.kbk.presentation.keyboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kbk.domain.irepository.IMotionRepository
import com.kbk.domain.iservice.IBiometricService
import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.TouchData
import kotlinx.coroutines.launch

class KeyboardViewModel(
    private val biometricService: IBiometricService,
    private val motionTracker: IMotionRepository
) : ViewModel() {
    private var lastUpTime: Long = 0L

    fun startTracking() {
        lastUpTime = 0L
        motionTracker.startTracking()
    }

    fun stopTracking() {
        motionTracker.stopTracking()
    }

    fun onKeyTouchRecorded(touchData: TouchData) {
        val currentMotion = motionTracker.motionState.value

        val sample = BiometricSample(
            touchData = touchData,
            motionData = currentMotion
        )

        viewModelScope.launch {
            biometricService.processAndSaveSample(sample)
        }
    }

    fun calculateFlightTime(downTime: Long): Long {
        val flight = if (lastUpTime == 0L) 0L else downTime - lastUpTime
        return flight
    }

    fun updateLastUpTime(upTime: Long) {
        lastUpTime = upTime
    }
}

