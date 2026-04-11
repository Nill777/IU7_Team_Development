package com.kbk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kbk.data.local.dao.BiometricSampleDao
import com.kbk.data.local.entity.BiometricSampleEntity

@Database(
    entities = [BiometricSampleEntity::class],
    version = 2,
    exportSchema = false
)
abstract class BiometricDatabase : RoomDatabase() {
    abstract fun biometricSampleDao(): BiometricSampleDao
}
