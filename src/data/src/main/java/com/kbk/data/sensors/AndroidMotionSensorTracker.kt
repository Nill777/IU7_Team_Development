package com.kbk.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.kbk.domain.irepository.IMotionSensorTracker
import com.kbk.domain.models.MotionData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidMotionSensorTracker(
    context: Context
) : IMotionSensorTracker, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _motionState = MutableStateFlow(
        MotionData(
            timestamp = 0L,
            accX = 0f,
            accY = 0f,
            accZ = 0f,
            gyroX = 0f,
            gyroY = 0f,
            gyroZ = 0f
        )
    )
    override val motionState: StateFlow<MotionData> = _motionState.asStateFlow()

    private val currentAcc = FloatArray(ARRAY_SIZE)
    private val currentGyro = FloatArray(ARRAY_SIZE)

    override fun startTracking() {
        accelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        gyroSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun stopTracking() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var isUpdated = false

        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                currentAcc[AXIS_X] = event.values[AXIS_X]
                currentAcc[AXIS_Y] = event.values[AXIS_Y]
                currentAcc[AXIS_Z] = event.values[AXIS_Z]
                isUpdated = true
            }

            Sensor.TYPE_GYROSCOPE -> {
                currentGyro[AXIS_X] = event.values[AXIS_X]
                currentGyro[AXIS_Y] = event.values[AXIS_Y]
                currentGyro[AXIS_Z] = event.values[AXIS_Z]
                isUpdated = true
            }
        }

        if (isUpdated) {
            _motionState.update {
                MotionData(
                    timestamp = System.currentTimeMillis(),
                    accX = currentAcc[AXIS_X],
                    accY = currentAcc[AXIS_Y],
                    accZ = currentAcc[AXIS_Z],
                    gyroX = currentGyro[AXIS_X],
                    gyroY = currentGyro[AXIS_Y],
                    gyroZ = currentGyro[AXIS_Z]
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // изменение точности сенсора
    }

    private companion object {
        const val ARRAY_SIZE = 3
        const val AXIS_X = 0
        const val AXIS_Y = 1
        const val AXIS_Z = 2
    }
}
