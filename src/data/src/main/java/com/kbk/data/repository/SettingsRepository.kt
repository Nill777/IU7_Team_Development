package com.kbk.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.kbk.domain.irepository.ISettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(context: Context) : ISettingsRepository {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("keystroke_settings", Context.MODE_PRIVATE)

    private val _batchSize = MutableStateFlow(prefs.getInt("batch_size", 4))
    override val batchSize: StateFlow<Int> = _batchSize.asStateFlow()

    private val _timingThreshold = MutableStateFlow(prefs.getFloat("timing_threshold", 4.0f))
    override val timingThreshold: StateFlow<Float> = _timingThreshold.asStateFlow()

    private val _spatialThreshold = MutableStateFlow(prefs.getFloat("spatial_threshold", 5.0f))
    override val spatialThreshold: StateFlow<Float> = _spatialThreshold.asStateFlow()

    private val _motionThreshold = MutableStateFlow(prefs.getFloat("motion_threshold", 6.0f))
    override val motionThreshold: StateFlow<Float> = _motionThreshold.asStateFlow()

    override suspend fun setBatchSize(size: Int) {
        prefs.edit { putInt("batch_size", size) }
        _batchSize.value = size
    }

    override suspend fun setTimingThreshold(value: Float) {
        prefs.edit { putFloat("timing_threshold", value) }
        _timingThreshold.value = value
    }

    override suspend fun setSpatialThreshold(value: Float) {
        prefs.edit { putFloat("spatial_threshold", value) }
        _spatialThreshold.value = value
    }

    override suspend fun setMotionThreshold(value: Float) {
        prefs.edit { putFloat("motion_threshold", value) }
        _motionThreshold.value = value
    }
}
