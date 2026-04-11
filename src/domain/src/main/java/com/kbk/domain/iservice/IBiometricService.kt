package com.kbk.domain.iservice

import com.kbk.domain.models.TouchData

interface IBiometricService {
    suspend fun saveSample(touch: TouchData)
    fun startBiometricCollection()
    fun stopBiometricCollection()
}
