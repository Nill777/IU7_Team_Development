package com.kbk.data.repository

import android.util.Log
import com.kbk.domain.irepository.IBiometricRepository
import com.kbk.domain.models.BiometricSample

class BiometricRepository : IBiometricRepository {
    override suspend fun saveSample(sample: BiometricSample) {
        Log.d(
            "BiometricRepo",
            "Saved sample: Key='${sample.touchData.key}', " +
                    "Dwell=${sample.touchData.dwellTime}ms, " +
                    "AccX=${sample.motionData.accX}"
        )
    }
}
