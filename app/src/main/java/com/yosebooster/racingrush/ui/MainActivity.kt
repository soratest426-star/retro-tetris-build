package com.yosebooster.racingrush.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yosebooster.racingrush.ui.models.MovementInput.Accelerometer
import com.yosebooster.racingrush.ui.screens.SecurityBlockScreen
import com.yosebooster.racingrush.ui.theme.RacingRushTheme
import com.yosebooster.racingrush.ui.viewmodel.MainViewModel
import com.yosebooster.racingrush.utils.SecurityUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SensorEventListener {
    private val viewModel by viewModels<MainViewModel>()

    private val sensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val accelerometer by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupCollectors()

        setContent {
            // Run anti-hack checks and remember results
            val violations = remember {
                val list = mutableListOf<String>()
                if (SecurityUtils.isDeviceRooted()) list.add("Dispositivo ruteado (Root detected)")
                if (SecurityUtils.isRunningOnEmulator()) list.add("Ejecución en Emulador (Emulator detected)")
                if (SecurityUtils.isFridaDetected()) list.add("Inyección dinámica Frida activa (Frida detected)")
                if (SecurityUtils.isXposedDetected()) list.add("Framework Xposed activo (Xposed detected)")
                if (SecurityUtils.isDebuggerAttached()) list.add("Depurador activo conectado (Debugger active)")
                if (SecurityUtils.isCheatAppInstalled(this@MainActivity)) list.add("App de trampas detectada (Cheat app detected)")
                list
            }
            val isDebuggable = remember { SecurityUtils.isDebuggable(this@MainActivity) }
            var bypassed by remember { mutableStateOf(false) }
            val securityViolation by viewModel.securityViolationEvent.collectAsState(initial = null)

            RacingRushTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (violations.isNotEmpty() && !bypassed) {
                        SecurityBlockScreen(
                            detectedViolations = violations,
                            isDebuggable = isDebuggable,
                            onBypass = { bypassed = true }
                        )
                    } else if (securityViolation != null) {
                        SecurityBlockScreen(
                            detectedViolations = listOf(securityViolation!!),
                            isDebuggable = isDebuggable,
                            onBypass = { /* In-game violation, no easy bypass */ }
                        )
                    } else {
                        RacingRushGameNavHost()
                    }
                }
            }
        }
    }

    private fun setupCollectors() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.movementInput.collect {
                    when (it) {
                        Accelerometer -> registerAccelerometer()
                        else -> unregisterAccelerometer()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerAccelerometer()
        viewModel.playBackgroundMusic()
    }

    private fun registerAccelerometer() {
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI   // SENSOR_DELAY_GAME was too much!
        )
    }

    private fun unregisterAccelerometer() {
        sensorManager.unregisterListener(this)
    }

    override fun onPause() {
        super.onPause()
        unregisterAccelerometer()
        viewModel.stopBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releaseSounds()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val accelerationX = event.values[0]
        val accelerationY = event.values[1]
        val accelerationZ = event.values[2]

        viewModel.setAcceleration(accelerationX, accelerationY, accelerationZ)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
