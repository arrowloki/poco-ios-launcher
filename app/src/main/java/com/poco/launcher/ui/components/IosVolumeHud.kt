package com.poco.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun IosVolumeHud(
    volumeRatio: Float?,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = volumeRatio != null
    val clampedRatio = (volumeRatio ?: 0f).coerceIn(0f, 1f)

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(140)) + slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(180)),
        exit = fadeOut(tween(220)) + slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(240)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .width(44.dp)
                .height(180.dp)
                .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(0.4f))
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xD92C2C2E))
                .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaRatio = -dragAmount.y / size.height
                        onVolumeChange((clampedRatio + deltaRatio).coerceIn(0f, 1f))
                    }
                }
        ) {
            BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                val totalHeight = maxHeight

                // Filled active white track
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(totalHeight * clampedRatio)
                        .background(Color.White)
                )

                // Speaker icon reflecting volume level
                val speakerIcon = when {
                    clampedRatio <= 0.02f -> Icons.AutoMirrored.Rounded.VolumeMute
                    clampedRatio < 0.45f -> Icons.AutoMirrored.Rounded.VolumeDown
                    else -> Icons.AutoMirrored.Rounded.VolumeUp
                }

                val iconTint = if (clampedRatio > 0.25f) Color(0xFF1C1C1E) else Color.White

                Icon(
                    imageVector = speakerIcon,
                    contentDescription = "Volume",
                    tint = iconTint,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .size(22.dp)
                )
            }
        }
    }
}
