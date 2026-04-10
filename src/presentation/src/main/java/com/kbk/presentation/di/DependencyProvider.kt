package com.kbk.presentation.di

import com.kbk.domain.irepository.IMotionRepository
import com.kbk.domain.iservice.IBiometricService

interface DependencyProvider {
    val biometricService: IBiometricService
    val motionRepository: IMotionRepository
}
