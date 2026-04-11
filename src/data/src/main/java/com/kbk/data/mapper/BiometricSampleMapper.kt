package com.kbk.data.mapper

import com.kbk.data.local.entity.BiometricSampleEntity
import com.kbk.domain.models.BiometricSample

fun BiometricSample.toEntity(): BiometricSampleEntity {
    return BiometricSampleEntity(
        key = this.touchData.key,
        dwellTime = this.touchData.dwellTime,
        flightTime = this.touchData.flightTime,
        pressure = this.touchData.pressure,
        touchX = this.touchData.touchX,
        touchY = this.touchData.touchY,
        swipeVectorX = this.touchData.swipeVectorX,
        swipeVectorY = this.touchData.swipeVectorY,
        timestamp = this.motionData.timestamp,
        accX = this.motionData.accX,
        accY = this.motionData.accY,
        accZ = this.motionData.accZ,
        gyroX = this.motionData.gyroX,
        gyroY = this.motionData.gyroY,
        gyroZ = this.motionData.gyroZ,
        rotVecX = this.motionData.rotVecX,
        rotVecY = this.motionData.rotVecY,
        rotVecZ = this.motionData.rotVecZ
    )
}
