package com.poco.launcher.ui.components

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.poco.launcher.core.widget.LauncherWidgetHost

@Composable
fun SquircleWidgetView(
    widgetId: Int,
    widgetHost: LauncherWidgetHost,
    isEditMode: Boolean = false,
    onRemoveClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val widgetInfo = remember(widgetId) { appWidgetManager.getAppWidgetInfo(widgetId) }

    val isDark = isSystemInDarkTheme()
    val widgetBg = if (isDark) Color(0x662C2C2E) else Color(0x80FFFFFF)
    val widgetBorder = if (isDark) Color(0x33FFFFFF) else Color(0x26000000)

    val infiniteTransition = rememberInfiniteTransition(label = "widget_wobble")
    val wobbleRotation by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 145, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "widget_wobble_rot"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .graphicsLayer {
                rotationZ = if (isEditMode) wobbleRotation else 0f
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(0.2f))
                .clip(RoundedCornerShape(22.dp))
                .background(widgetBg)
                .border(0.5.dp, widgetBorder, RoundedCornerShape(22.dp))
                .padding(8.dp)
        ) {
            if (widgetInfo != null) {
                AndroidView(
                    factory = { ctx ->
                        widgetHost.createView(ctx, widgetId, widgetInfo).apply {
                            setAppWidget(widgetId, widgetInfo)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Widget",
                        color = if (isDark) Color.White else Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // (-) Delete Widget Badge in Edit Mode
        if (isEditMode && onRemoveClick != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-3).dp, y = (-3).dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xE648484A))
                    .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .clickable { onRemoveClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 10.dp, height = 2.dp)
                        .background(Color.White, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}
