package com.kbk.data.repository

import android.util.Log
import com.kbk.data.local.dao.BiometricSampleDao
import com.kbk.data.mapper.toEntity
import com.kbk.domain.irepository.IBiometricRepository
import com.kbk.domain.models.BiometricSample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BiometricRepository(private val dao: BiometricSampleDao) : IBiometricRepository {
    override suspend fun saveSample(sample: BiometricSample) {
        withContext(Dispatchers.IO) {
            val entity = sample.toEntity()
            dao.insertSample(entity)

            Log.d(
                "BiometricRepo",
                "Saved sample: Key='${sample.touchData.key}', " +
                        "Dwell=${sample.touchData.dwellTime}ms, " +
                        "AccX=${sample.motionData.accX}"
            )
        }
    }
}
