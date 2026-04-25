package com.kbk

import android.app.Application
import androidx.room.Room
import com.kbk.data.local.BiometricDatabase
import com.kbk.data.repository.BiometricRepository
import com.kbk.data.repository.SettingsRepository
import com.kbk.data.sensors.AndroidMotionRepository
import com.kbk.domain.irepository.IBiometricRepository
import com.kbk.domain.irepository.IMotionRepository
import com.kbk.domain.irepository.ISettingsRepository
import com.kbk.domain.isdk.IKeystrokeVerificationManager
import com.kbk.domain.iservice.IBiometricService
import com.kbk.domain.service.BiometricService
import com.kbk.keystrokeSdk.ensemble.KeystrokeVerificationManager
import com.kbk.presentation.di.DependencyProvider

class KeystrokeApplication : Application(), DependencyProvider {
    private val motionRepository: IMotionRepository by lazy {
        AndroidMotionRepository(this)
    }
    private val database: BiometricDatabase by lazy {
        Room.databaseBuilder(
            this,
            BiometricDatabase::class.java,
            "biometric_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    private val biometricRepository: IBiometricRepository by lazy {
        BiometricRepository(database.biometricSampleDao())
    }

    private val verificationManager: IKeystrokeVerificationManager by lazy {
        KeystrokeVerificationManager()
    }

    override val settingsRepository: ISettingsRepository by lazy {
        SettingsRepository(this)
    }

    override val biometricService: IBiometricService by lazy {
        BiometricService(biometricRepository, motionRepository, verificationManager, settingsRepository)
    }
}
