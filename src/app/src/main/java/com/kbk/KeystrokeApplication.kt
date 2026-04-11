package com.kbk

import android.app.Application
import com.kbk.data.repository.BiometricRepository
import com.kbk.data.sensors.AndroidMotionRepository
import com.kbk.domain.irepository.IBiometricRepository
import com.kbk.domain.irepository.IMotionRepository
import com.kbk.domain.iservice.IBiometricService
import com.kbk.domain.service.BiometricService
import com.kbk.presentation.di.DependencyProvider

class KeystrokeApplication : Application(), DependencyProvider {
    private val motionRepository: IMotionRepository by lazy {
        AndroidMotionRepository(this)
    }
    private val biometricRepository: IBiometricRepository by lazy {
        BiometricRepository()
    }

    override val biometricService: IBiometricService by lazy {
        BiometricService(biometricRepository, motionRepository)
    }
}
