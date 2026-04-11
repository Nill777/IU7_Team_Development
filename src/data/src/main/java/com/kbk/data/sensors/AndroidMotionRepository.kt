package com.kbk.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
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

    private var sensorThread: HandlerThread? = null
    private var sensorHandler: Handler? = null

    private val _motionState = MutableStateFlow(MotionData())
    override val motionState: StateFlow<MotionData> = _motionState.asStateFlow()

    companion object {
        private const val AXIS_COUNT = 3
    }

    private val curAcc = FloatArray(AXIS_COUNT)
    private val curGyro = FloatArray(AXIS_COUNT)
    private val curRot = FloatArray(AXIS_COUNT)

    override fun startTracking() {
        if (sensorThread == null) {
            val thread = HandlerThread("SensorThread").apply { start() }
            sensorThread = thread
            sensorHandler = Handler(thread.looper)
        }

        listOf(accelSensor, gyroSensor, rotVecSensor).forEach { sensor ->
            sensor?.let {
                sensorManager.registerListener(
                    this,
                    it,
                    SensorManager.SENSOR_DELAY_FASTEST,
                    sensorHandler
                )
            }
        }
    }

    override fun stopTracking() {
        sensorManager.unregisterListener(this)
        sensorThread?.quitSafely()
        sensorThread = null
        sensorHandler = null

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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //
    }
}
