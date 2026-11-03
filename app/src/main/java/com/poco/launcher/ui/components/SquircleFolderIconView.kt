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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun SquircleFolderIconView(
    title: String,
    apps: List<AppItem>,
    iconSize: Dp = 60.dp,
    showLabel: Boolean = true,
    textColor: Color = Color.White,
    isEditMode: Boolean = false,
    onRemoveClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val folderBg = if (isDark) Color(0x662C2C2E) else Color(0x66FFFFFF)
    val folderBorder = if (isDark) Color(0x33FFFFFF) else Color(0x33000000)

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isEditMode) 0.88f else 1.0f,
        label = "folder_press_scale"
    )

    // Jiggle / Wobble Animation in Edit Mode
    val infiniteTransition = rememberInfiniteTransition(label = "folder_wobble")
    val wobbleRotation by infiniteTransition.animateFloat(
        initialValue = if ((title.hashCode() and 1) == 0) -1.8f else 1.8f,
        targetValue = if ((title.hashCode() and 1) == 0) 1.8f else -1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 140, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "folder_wobble_rot"
    )

    // Take up to 9 apps for the 3x3 mini preview
    val previewApps = remember(apps) { apps.take(9) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(iconSize + 22.dp)
            .padding(vertical = 4.dp)
            .scale(scale)
            .graphicsLayer {
                rotationZ = if (isEditMode) wobbleRotation else 0f
            }
            .pointerInput(title, isEditMode) {
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
                        }
                    },
                    onLongPress = { onLongClick?.invoke() }
                )
            }
    ) {
        Box(
            modifier = Modifier.size(iconSize)
        ) {
            // iOS-style translucent squircle container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(iconSize)
                    .clip(SquircleShape(0.24f))
                    .background(folderBg)
                    .border(0.5.dp, folderBorder, SquircleShape(0.24f))
                    .padding(6.dp)
            ) {
                // 3x3 mini preview grid
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.size(iconSize - 12.dp)
                ) {
                    for (row in 0 until 3) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(iconSize - 12.dp)
                        ) {
                            for (col in 0 until 3) {
                                val index = row * 3 + col
                                val miniApp = previewApps.getOrNull(index)
                                val miniSize = (iconSize / 4.4f).coerceAtLeast(10.dp)

                                if (miniApp != null) {
                                    MiniAppIcon(app = miniApp, size = miniSize)
                                } else {
                                    Spacer(modifier = Modifier.size(miniSize))
                                }
                            }
                        }
                    }
                }
            }

            // iOS-style (-) Delete / Unpack Folder Badge in Edit Mode
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
                text = title,
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

@Composable
private fun MiniAppIcon(
    app: AppItem,
    size: Dp
) {
    val iconCacheManager = remember { PocoLauncherApp.instance.iconCacheManager }
    val initialBitmap = remember(app.id) { iconCacheManager.getCachedIcon(app.id) }

    val iconBitmap by produceState<Bitmap?>(initialValue = initialBitmap, key1 = app.id) {
        if (value == null) {
            value = iconCacheManager.getAppIcon(app)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(SquircleShape(0.22f))
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(size)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    }
}
