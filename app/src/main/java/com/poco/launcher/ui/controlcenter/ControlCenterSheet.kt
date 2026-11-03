package com.poco.launcher.ui.controlcenter

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NetworkCell
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poco.launcher.PocoLauncherApp
import com.poco.launcher.ui.theme.IosColors

@Composable
fun ControlCenterSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val context = LocalContext.current
    val view = LocalView.current
    val activity = context as? Activity
    val sys = remember { PocoLauncherApp.instance.systemControllers }

    val isDark = isSystemInDarkTheme()
    val overlayBg = if (isDark) Color(0xD9000000) else Color(0xB31C1C1E)

    val isTorchOn by sys.isTorchOn.collectAsState()
    val batteryLevel by sys.batteryLevel.collectAsState()
    val isCharging by sys.isCharging.collectAsState()

    var brightnessRatio by remember { mutableFloatStateOf(sys.getCurrentSystemBrightnessRatio()) }
    var volumeRatio by remember {
        mutableFloatStateOf(sys.currentMusicVolume.toFloat() / sys.maxMusicVolume.coerceAtLeast(1))
    }

    var isWifiActive by remember { mutableStateOf(sys.isWifiEnabled()) }
    var isBluetoothActive by remember { mutableStateOf(sys.isBluetoothEnabled()) }
    var isAutoRotateActive by remember { mutableStateOf(sys.isAutoRotateEnabled()) }
    var isDndActive by remember { mutableStateOf(sys.isDndEnabled()) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            sys.updateBatteryState()
            brightnessRatio = sys.getCurrentSystemBrightnessRatio()
            volumeRatio = sys.currentMusicVolume.toFloat() / sys.maxMusicVolume.coerceAtLeast(1)
            isWifiActive = sys.isWifiEnabled()
            isBluetoothActive = sys.isBluetoothEnabled()
            isAutoRotateActive = sys.isAutoRotateEnabled()
            isDndActive = sys.isDndEnabled()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(300)),
        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(260)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayBg)
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                }
                .pointerInput(Unit) {
                    // Swipe up to dismiss Control Center
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            if (dragAmount < -18f) {
                                change.consume()
                                onDismiss()
                            }
                        }
                    )
                }
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        // Prevent dismiss when tapping inside the control platter area
                        detectTapGestures { }
                    }
            ) {
                // Top Status Bar: Title & Battery telemetry
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "POCO Control Center",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryFull,
                            contentDescription = "Battery",
                            tint = if (isCharging) IosColors.Green else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$batteryLevel%",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 1: 2x2 Connectivity Platter & Vertical Sliders (Brightness + Volume)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    // 2x2 Connectivity Platter
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0x38FFFFFF))
                            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ControlTileIcon(
                                    icon = Icons.Rounded.AirplanemodeActive,
                                    label = "Airplane",
                                    isActive = false,
                                    activeColor = IosColors.Orange,
                                    onClick = {
                                        sys.performHaptic(view)
                                        sys.openAirplaneModeSettings()
                                    }
                                )
                                ControlTileIcon(
                                    icon = Icons.Rounded.NetworkCell,
                                    label = "Cellular",
                                    isActive = true,
                                    activeColor = IosColors.Green,
                                    onClick = {
                                        sys.performHaptic(view)
                                        sys.openMobileDataPanel()
                                    }
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ControlTileIcon(
                                    icon = Icons.Rounded.Wifi,
                                    label = "Wi-Fi",
                                    isActive = isWifiActive,
                                    activeColor = IosColors.Blue,
                                    onClick = {
                                        sys.performHaptic(view)
                                        sys.openWifiPanel()
                                    }
                                )
                                ControlTileIcon(
                                    icon = Icons.Rounded.Bluetooth,
                                    label = "Bluetooth",
                                    isActive = isBluetoothActive,
                                    activeColor = IosColors.Blue,
                                    onClick = {
                                        sys.performHaptic(view)
                                        sys.openBluetoothSettings()
                                    }
                                )
                            }
                        }
                    }

                    // Sliders Row: Brightness Slider & Volume Slider
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Brightness Slider
                        VerticalControlSlider(
                            value = brightnessRatio,
                            onValueChange = {
                                brightnessRatio = it
                                if (activity != null) {
                                    sys.applyWindowBrightness(activity, it)
                                }
                            },
                            icon = Icons.Rounded.LightMode,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        // Volume Slider
                        VerticalControlSlider(
                            value = volumeRatio,
                            onValueChange = {
                                volumeRatio = it
                                val newVolume = (it * sys.maxMusicVolume).toInt()
                                sys.setMusicVolume(newVolume)
                            },
                            icon = Icons.AutoMirrored.Rounded.VolumeUp,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Row 2: Secondary Quick Toggles (Flashlight, Auto-Rotate, DND)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SquareControlTile(
                        icon = Icons.Rounded.FlashlightOn,
                        label = if (isTorchOn) "Torch On" else "Torch",
                        isActive = isTorchOn,
                        activeColor = IosColors.Blue,
                        onClick = {
                            sys.performHaptic(view)
                            sys.toggleTorch()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SquareControlTile(
                        icon = Icons.Rounded.ScreenRotation,
                        label = "Rotation",
                        isActive = isAutoRotateActive,
                        activeColor = IosColors.Red,
                        onClick = {
                            sys.performHaptic(view)
                            sys.toggleAutoRotate()
                            isAutoRotateActive = sys.isAutoRotateEnabled()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SquareControlTile(
                        icon = Icons.Rounded.DoNotDisturbOn,
                        label = "Focus / DND",
                        isActive = isDndActive,
                        activeColor = IosColors.Indigo,
                        onClick = {
                            sys.performHaptic(view)
                            sys.toggleDnd()
                            isDndActive = sys.isDndEnabled()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Row 3: Quick Tool Utilities (Calculator, Camera, Settings)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SquareControlTile(
                        icon = Icons.Rounded.Calculate,
                        label = "Calculator",
                        isActive = false,
                        activeColor = IosColors.DarkGray,
                        onClick = {
                            sys.performHaptic(view)
                            onDismiss()
                            sys.openCalculator()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SquareControlTile(
                        icon = Icons.Rounded.CameraAlt,
                        label = "Camera",
                        isActive = false,
                        activeColor = IosColors.DarkGray,
                        onClick = {
                            sys.performHaptic(view)
                            onDismiss()
                            sys.openCamera()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SquareControlTile(
                        icon = Icons.Rounded.Settings,
                        label = "Settings",
                        isActive = false,
                        activeColor = IosColors.DarkGray,
                        onClick = {
                            sys.performHaptic(view)
                            onDismiss()
                            if (onOpenSettings != null) {
                                onOpenSettings()
                            } else {
                                sys.openSystemSettings()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalControlSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val clamped = value.coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0x403A3A3C))
            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val ratio = 1f - (offset.y / size.height)
                    onValueChange(ratio.coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val deltaRatio = -dragAmount.y / size.height
                        onValueChange((clamped + deltaRatio).coerceIn(0f, 1f))
                    }
                )
            }
    ) {
        val sliderHeight = maxHeight

        // Filled active track
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(sliderHeight * clamped)
                .background(Color.White)
        )

        // Bottom Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (clamped > 0.35f) Color(0xFF1C1C1E) else Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .size(24.dp)
        )
    }
}

@Composable
private fun ControlTileIcon(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val bg = if (isActive) activeColor else Color(0x33767680)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun SquareControlTile(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) activeColor else Color(0x403A3A3C)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
