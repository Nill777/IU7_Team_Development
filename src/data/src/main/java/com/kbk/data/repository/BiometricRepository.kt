package com.kbk.data.repository

import android.util.Log
import com.kbk.data.local.dao.BiometricSampleDao
import com.kbk.data.mapper.toDomain
import com.kbk.data.mapper.toEntity
import com.kbk.domain.irepository.IBiometricRepository
import com.kbk.domain.models.BiometricSample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class BiometricRepository(private val dao: BiometricSampleDao) : IBiometricRepository {

    companion object {
        private const val MAX_RETENTION_PER_KEY = 200
        private const val CLEANUP_THRESHOLD = 25
        private const val TOTAL_LIMIT_BEFORE_CLEANUP = MAX_RETENTION_PER_KEY + CLEANUP_THRESHOLD
    }

    // кол-во записей в БД для каждой клавиши
    private val keyRecordsCount = ConcurrentHashMap<String, Int>()

    // флаги, подгрузили ли начальное значение из БД для этой клавиши
    private val isCountInitialized = ConcurrentHashMap<String, Boolean>()

    override suspend fun saveSample(sample: BiometricSample) {
        withContext(Dispatchers.IO) {
            val key = sample.touchData.key

            if (isCountInitialized[key] != true) {
                val countInDb = dao.getCountForKey(key)
                keyRecordsCount[key] = countInDb
                isCountInitialized[key] = true
            }

            dao.insertSample(sample.toEntity())
            Log.d(
                "BiometricRepo",
                "Saved sample: Key='$key', " +
                        "Dwell=${sample.touchData.dwellTime}ms, " +
                        "AccX=${sample.motionData.accX}"
            )


            val newTotal = (keyRecordsCount[key] ?: 0) + 1
            keyRecordsCount[key] = newTotal

            if (newTotal >= TOTAL_LIMIT_BEFORE_CLEANUP) {
                dao.cleanupOldSamples(key, MAX_RETENTION_PER_KEY)

                // после чистки в БД гарантированно осталось ровно 200 записей
                keyRecordsCount[key] = MAX_RETENTION_PER_KEY

                Log.d(
                    "BiometricRepo",
                    "Database cleanup: Key='$key' reached $newTotal records. Truncated to $MAX_RETENTION_PER_KEY"
                )
            }
        }
    }

    override fun getAllSamples(): Flow<List<BiometricSample>> {
        return dao.getAllSamples().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getSamplesCount(): Int = dao.getSamplesCount()
}
