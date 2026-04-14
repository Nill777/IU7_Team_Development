package com.kbk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kbk.data.local.entity.BiometricSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BiometricSampleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: BiometricSampleEntity)

    @Query("SELECT * FROM biometric_samples ORDER BY timestamp ASC")
    fun getAllSamples(): Flow<List<BiometricSampleEntity>>
}
