package com.poco.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.poco.launcher.core.model.AppItem
import com.poco.launcher.ui.theme.IosColors

@Composable
fun DockBar(
    dockApps: List<AppItem>,
    iconSize: Dp = 58.dp,
    isEditMode: Boolean = false,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: ((AppItem) -> Unit)? = null,
    onRemoveClick: ((AppItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val dockBg = if (isDark) IosColors.DarkDock else IosColors.LightDock
    val dockBorder = if (isDark) IosColors.DarkBorder else IosColors.LightBorder

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.25f))
                .clip(RoundedCornerShape(32.dp))
                .background(dockBg)
                .border(width = 0.6.dp, color = dockBorder, shape = RoundedCornerShape(32.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            dockApps.forEach { app ->
                SquircleIconView(
                    app = app,
                    iconSize = iconSize,
                    showLabel = false, // iOS dock never displays labels
                    isEditMode = isEditMode,
                    onRemoveClick = onRemoveClick?.let { { it(app) } },
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick?.invoke(app) }
                )
            }
        }
    }
}
