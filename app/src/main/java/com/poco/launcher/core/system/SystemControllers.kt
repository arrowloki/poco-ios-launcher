package com.poco.launcher.core.system

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Clean Android system hardware & settings controller.
 * Adheres strictly to Android permissions and avoids fake mockups.
 */
class SystemControllers(private val context: Context) {

    // --- Audio Control ---
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val maxMusicVolume: Int
        get() = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    val currentMusicVolume: Int
        get() = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    fun setMusicVolume(volume: Int) {
        val clamped = volume.coerceIn(0, maxMusicVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, 0)
    }

    // --- Flashlight / Torch Control ---
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var torchCameraId: String? = null
    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    init {
        initTorch()
    }

    private fun initTorch() {
        if (cameraManager == null) return
        try {
            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    torchCameraId = id
                    break
                }
            }

            cameraManager.registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    if (cameraId == torchCameraId) {
                        _isTorchOn.value = enabled
                    }
                }
            }, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleTorch(): Boolean {
        val id = torchCameraId ?: return false
        val newState = !_isTorchOn.value
        return try {
            cameraManager?.setTorchMode(id, newState)
            _isTorchOn.value = newState
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- Screen Brightness Control ---
    fun getCurrentSystemBrightnessRatio(): Float {
        return try {
            val brightnessInt = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            (brightnessInt / 255f).coerceIn(0.01f, 1f)
        } catch (e: Exception) {
            0.5f
        }
    }

    fun applyWindowBrightness(activity: Activity, ratio: Float) {
        val clamped = ratio.coerceIn(0.01f, 1f)
        val lp = activity.window.attributes
        lp.screenBrightness = clamped
        activity.window.attributes = lp
    }

    // --- Battery Information ---
    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    fun updateBatteryState() {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
        if (batteryStatus != null) {
            val level: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                _batteryLevel.value = ((level / scale.toFloat()) * 100).toInt()
            }
            val status: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        }
    }

    // --- Connectivity & Quick Settings Panels ---
    fun openWifiPanel() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_WIFI)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openBluetoothSettings() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openAirplaneModeSettings() {
        val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openMobileDataPanel() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        } else {
            Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openDisplaySettings() {
        val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun openCalculator() {
        val calculatorIntents = listOf(
            Intent().setClassName("com.miui.calculator", "com.miui.calculator.cal.CalculatorActivity"),
            Intent().setClassName("com.google.android.calculator", "com.android.calculator2.Calculator"),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)
        )
        for (intent in calculatorIntents) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return
            }
        }
        // Fallback: search general calculator
        val generic = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(generic)
        } catch (e: Exception) {
            openSystemSettings()
        }
    }

    fun openSystemSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
