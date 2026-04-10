package com.kbk

import android.app.Application
import com.kbk.data.repository.BiometricRepository
import com.kbk.data.sensors.AndroidMotionRepository
import com.kbk.domain.irepository.IMotionRepository
import com.kbk.domain.service.BiometricService
import com.kbk.presentation.di.DependencyProvider

class KeystrokeApplication : Application(), DependencyProvider {
    override val motionRepository: IMotionRepository by lazy {
        AndroidMotionRepository(this)
    }

    private val biometricRepository by lazy { BiometricRepository() }

    override val biometricService: BiometricService by lazy {
        BiometricService(biometricRepository)
    }
}
