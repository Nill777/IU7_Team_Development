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

    @Query("SELECT COUNT(*) FROM biometric_samples WHERE `key` = :targetKey")
    suspend fun getCountForKey(targetKey: String): Int

    // оставляем N последних записей для конкретной клавиши, остальное удаляем
    @Query("""
        DELETE FROM biometric_samples 
        WHERE `key` = :targetKey AND id NOT IN (
            SELECT id FROM biometric_samples 
            WHERE `key` = :targetKey 
            ORDER BY timestamp DESC 
            LIMIT :keepCount
        )
    """)
    suspend fun cleanupOldSamples(targetKey: String, keepCount: Int)
}
