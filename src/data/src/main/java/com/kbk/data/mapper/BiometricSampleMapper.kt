package com.kbk.data.mapper

import com.kbk.data.local.entity.BiometricSampleEntity
import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.MotionData
import com.kbk.domain.models.TouchData

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
        rotVecZ = this.motionData.rotVecZ,
        gravX = this.motionData.gravX,
        gravY = this.motionData.gravY,
        gravZ = this.motionData.gravZ
    )
}

fun BiometricSampleEntity.toDomain(): BiometricSample {
    return BiometricSample(
        touchData = TouchData(
            key,
            dwellTime,
            flightTime,
            pressure,
            touchX,
            touchY,
            swipeVectorX,
            swipeVectorY
        ),
        motionData = MotionData(
            timestamp,
            accX,
            accY,
            accZ,
            gyroX,
            gyroY,
            gyroZ,
            rotVecX,
            rotVecY,
            rotVecZ,
            gravX,
            gravY,
            gravZ
        )
    )
}
