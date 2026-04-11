package com.kbk.presentation.di

import com.kbk.domain.iservice.IBiometricService

interface DependencyProvider {
    val biometricService: IBiometricService
}
