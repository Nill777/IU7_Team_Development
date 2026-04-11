package com.kbk.domain.service

import com.kbk.domain.irepository.IBiometricRepository
import com.kbk.domain.irepository.IMotionRepository
import com.kbk.domain.iservice.IBiometricService
import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.TouchData

class BiometricService(
    private val biometricRepository: IBiometricRepository,
    private val motionRepository: IMotionRepository
) : IBiometricService {
    override fun startBiometricCollection() = motionRepository.startTracking()
    override fun stopBiometricCollection() = motionRepository.stopTracking()

    override suspend fun saveSample(touch: TouchData) {
        val currentMotion = motionRepository.motionState.value

        val sample = BiometricSample(
            touchData = touch,
            motionData = currentMotion
        )

        biometricRepository.saveSample(sample)
    }
}
