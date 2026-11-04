package com.poco.launcher.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poco.launcher.PocoLauncherApp
import com.poco.launcher.core.icon.SquircleShape
import com.poco.launcher.core.model.AppItem

@Composable
fun SquircleIconView(
    app: AppItem,
    iconSize: Dp = 60.dp,
    showLabel: Boolean = true,
    textColor: Color = Color.White,
    isEditMode: Boolean = false,
    onRemoveClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onEditModeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val iconCacheManager = remember { PocoLauncherApp.instance.iconCacheManager }
    val initialBitmap = remember(app.id) { iconCacheManager.getCachedIcon(app.id) }

    val iconBitmap by produceState<Bitmap?>(initialValue = initialBitmap, key1 = app.id) {
        if (value == null) {
            value = iconCacheManager.getAppIcon(app)
        }
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isEditMode) 0.88f else 1.0f,
        label = "icon_press_scale"
    )

    // iOS Jiggle / Wobble Animation in Edit Mode
    val infiniteTransition = rememberInfiniteTransition(label = "icon_wobble")
    val wobbleRotation by infiniteTransition.animateFloat(
        initialValue = if ((app.id.hashCode() and 1) == 0) -1.8f else 1.8f,
        targetValue = if ((app.id.hashCode() and 1) == 0) 1.8f else -1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 135, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble_rot"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(iconSize + 22.dp)
            .padding(vertical = 4.dp)
            .scale(scale)
            .graphicsLayer {
                rotationZ = if (isEditMode) wobbleRotation else 0f
            }
            .pointerInput(app.id, isEditMode) {
                detectTapGestures(
                    onPress = {
                        if (!isEditMode) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    },
                    onTap = {
                        if (!isEditMode) {
                            onClick()
                        } else {
                            onEditModeClick?.invoke()
                        }
                    },
                    onLongPress = {
                        onLongClick?.invoke()
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier.size(iconSize)
        ) {
            // Squircle App Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(iconSize)
                    .clip(SquircleShape())
            ) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap!!.asImageBitmap(),
                        contentDescription = app.label,
                        modifier = Modifier.size(iconSize)
                    )
                } else {
                    Box(modifier = Modifier.size(iconSize))
                }
            }

            // iOS-style (-) Delete / Remove Badge in Edit Mode
            if (isEditMode && onRemoveClick != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-3).dp, y = (-3).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xE648484A))
                        .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        .clickable { onRemoveClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .width(9.dp)
                            .height(2.dp)
                            .background(Color.White, RoundedCornerShape(1.dp))
                    )
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.label,
                color = textColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.7f),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 3f
                    )
                ),
                modifier = Modifier.width(iconSize + 20.dp)
            )
        }
    }
}
