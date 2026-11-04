package com.poco.launcher.ui.controlcenter

import android.app.Activity
import android.view.KeyEvent
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
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NetworkCell
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

    val overlayBg = Color.Black.copy(alpha = 0.82f)

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
    var isPlaying by remember { mutableStateOf(false) }

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
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures { /* consume tap inside */ }
                    }
            ) {
                // Top Status Bar: Carrier, POCO title & Battery telemetry
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Control Center",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryFull,
                            contentDescription = "Battery",
                            tint = if (isCharging) IosColors.Green else Color.White,
                            modifier = Modifier.size(17.dp)
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

                // ROW 1: Connectivity Platter (2x2) & Now Playing Music Platter (2x2)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(156.dp)
                ) {
                    // 2x2 Connectivity Platter
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0x4D2C2C2E))
                            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                            .padding(12.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ControlCircleIcon(
                                    icon = Icons.Rounded.AirplanemodeActive,
                                    label = "Airplane",
                                    isActive = false,
                                    activeColor = IosColors.Orange,
                                    onClick = {
                                        sys.performHaptic(view)
                                        sys.openAirplaneModeSettings()
                                    }
                                )
                                ControlCircleIcon(
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
                                ControlCircleIcon(
                                    icon = Icons.Rounded.Wifi,
                                    label = "Wi-Fi",
                                    isActive = isWifiActive,
                                    activeColor = IosColors.Blue,
                                    onClick = {
                                        sys.performHaptic(view)
                                        sys.openWifiPanel()
                                    }
                                )
                                ControlCircleIcon(
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

                    // 2x2 Now Playing Music Platter (Signature iOS element)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0x4D2C2C2E))
                            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                            .padding(12.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x33FFFFFF))
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MusicNote,
                                        contentDescription = null,
                                        tint = IosColors.Red,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Music",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (isPlaying) "Now Playing" else "Not Playing",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Playback controls (Rewind, Play/Pause, Fast Forward)
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = {
                                        sys.performHaptic(view)
                                        sys.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipPrevious,
                                        contentDescription = "Previous",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x33FFFFFF))
                                        .clickable {
                                            sys.performHaptic(view)
                                            isPlaying = !isPlaying
                                            sys.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                                        }
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        sys.performHaptic(view)
                                        sys.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipNext,
                                        contentDescription = "Next",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ROW 2: Rotation Lock (1x1) & Focus/DND Pill (2x1)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    // Orientation Lock (1x1 square)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (isAutoRotateActive) Color(0x4D2C2C2E) else Color(0x4D2C2C2E))
                            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
                            .clickable {
                                sys.performHaptic(view)
                                sys.toggleAutoRotate()
                                isAutoRotateActive = sys.isAutoRotateEnabled()
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ScreenRotation,
                                contentDescription = "Rotation",
                                tint = if (!isAutoRotateActive) IosColors.Red else Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Focus / Do Not Disturb (Wide 2x1 iOS Pill)
                    Box(
                        modifier = Modifier
                            .weight(2.1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (isDndActive) IosColors.Indigo else Color(0x4D2C2C2E))
                            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
                            .clickable {
                                sys.performHaptic(view)
                                sys.toggleDnd()
                                isDndActive = sys.isDndEnabled()
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDndActive) Color.White.copy(alpha = 0.25f) else Color(0x33FFFFFF))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DarkMode,
                                    contentDescription = "Focus",
                                    tint = if (isDndActive) Color.White else IosColors.Indigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isDndActive) "Do Not Disturb" else "Focus",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isDndActive) "On" else "Off",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ROW 3: Authentic iOS Capsule Sliders (Brightness + Volume side by side)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    // Brightness Capsule Slider
                    IosCapsuleSlider(
                        value = brightnessRatio,
                        onValueChange = {
                            brightnessRatio = it
                            if (activity != null) {
                                sys.applyWindowBrightness(activity, it)
                            }
                        },
                        icon = Icons.Rounded.LightMode,
                        label = "Brightness",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    // Volume Capsule Slider with dynamic speaker icon
                    val volumeIcon = when {
                        volumeRatio <= 0.02f -> Icons.AutoMirrored.Rounded.VolumeMute
                        volumeRatio < 0.45f -> Icons.AutoMirrored.Rounded.VolumeDown
                        else -> Icons.AutoMirrored.Rounded.VolumeUp
                    }

                    IosCapsuleSlider(
                        value = volumeRatio,
                        onValueChange = {
                            volumeRatio = it
                            val newVolume = (it * sys.maxMusicVolume).toInt()
                            sys.setMusicVolume(newVolume)
                        },
                        icon = volumeIcon,
                        label = "Volume",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ROW 4: 4 Quick Action Square Tiles (Flashlight, Calculator, Camera, Settings)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IosSquareTile(
                        icon = Icons.Rounded.FlashlightOn,
                        label = "Torch",
                        isActive = isTorchOn,
                        activeColor = IosColors.Blue,
                        onClick = {
                            sys.performHaptic(view)
                            sys.toggleTorch()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    IosSquareTile(
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
                    IosSquareTile(
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
                    IosSquareTile(
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
private fun IosCapsuleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val clamped = value.coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0x4D2C2C2E))
            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(26.dp))
            .pointerInput(label) {
                detectTapGestures { offset ->
                    val ratio = 1f - (offset.y / size.height)
                    onValueChange(ratio.coerceIn(0f, 1f))
                }
            }
            .pointerInput(label) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val deltaRatio = -dragAmount.y / size.height
                    onValueChange((clamped + deltaRatio).coerceIn(0f, 1f))
                }
            }
    ) {
        val sliderHeight = maxHeight

        // Filled active track in iOS white
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(sliderHeight * clamped)
                .background(Color.White)
        )

        // Bottom Icon that transitions color as fill level rises
        val iconTint = if (clamped > 0.32f) Color(0xFF1C1C1E) else Color.White

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp)
                .size(26.dp)
        )
    }
}

@Composable
private fun ControlCircleIcon(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val bg = if (isActive) activeColor else Color(0x33FFFFFF)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun IosSquareTile(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) activeColor else Color(0x4D2C2C2E)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
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
