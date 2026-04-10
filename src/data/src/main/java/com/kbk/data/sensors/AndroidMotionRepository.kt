package com.kbk.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.kbk.domain.irepository.IMotionRepository
import com.kbk.domain.models.MotionData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidMotionRepository(context: Context) : IMotionRepository, SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val rotVecSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _motionState = MutableStateFlow(MotionData())
    override val motionState: StateFlow<MotionData> = _motionState.asStateFlow()

    private val curAcc = FloatArray(3)
    private val curGyro = FloatArray(3)
    private val curRot = FloatArray(3)

    override fun startTracking() {
        listOf(accelSensor, gyroSensor, rotVecSensor).forEach { sensor ->
            sensor?.let {
                sensorManager.registerListener(
                    this,
                    it,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
            }
        }
    }

    override fun stopTracking() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                curAcc[0] = event.values[0]; curAcc[1] = event.values[1]; curAcc[2] =
                    event.values[2]
            }

            Sensor.TYPE_GYROSCOPE -> {
                curGyro[0] = event.values[0]; curGyro[1] = event.values[1]; curGyro[2] =
                    event.values[2]
            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                curRot[0] = event.values[0]; curRot[1] = event.values[1]; curRot[2] =
                    event.values[2]
            }
        }
        _motionState.update {
            MotionData(
                timestamp = System.currentTimeMillis(),
                accX = curAcc[0], accY = curAcc[1], accZ = curAcc[2],
                gyroX = curGyro[0], gyroY = curGyro[1], gyroZ = curGyro[2],
                rotVecX = curRot[0], rotVecY = curRot[1], rotVecZ = curRot[2]
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
