package com.kbk.presentation.di

import com.kbk.domain.irepository.IMotionRepository
import com.kbk.domain.service.BiometricService

interface DependencyProvider {
    val biometricService: BiometricService
    val motionRepository: IMotionRepository
}
