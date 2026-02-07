package com.assgui.gourmandine.ui.screens.addreview

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccelerometerSensor(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _tiltX = MutableStateFlow(0f)
    val tiltX: StateFlow<Float> = _tiltX.asStateFlow()

    private val _tiltY = MutableStateFlow(0f)
    val tiltY: StateFlow<Float> = _tiltY.asStateFlow()

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        // x: -10..10 (tilt left/right), y: -10..10 (tilt forward/back)
        val x = event.values[0]
        val y = event.values[1]
        // Normalize to 0..1 range
        _tiltX.value = ((x + 10f) / 20f).coerceIn(0f, 1f)
        _tiltY.value = ((y + 10f) / 20f).coerceIn(0f, 1f)
    }

}
