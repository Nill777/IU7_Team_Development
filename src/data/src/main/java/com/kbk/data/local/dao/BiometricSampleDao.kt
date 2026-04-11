package com.kbk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.kbk.data.local.entity.BiometricSampleEntity

@Dao
interface BiometricSampleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: BiometricSampleEntity)
}
