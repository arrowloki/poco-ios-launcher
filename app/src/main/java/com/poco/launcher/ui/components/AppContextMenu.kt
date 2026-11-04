package com.poco.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poco.launcher.core.model.AppItem
import com.poco.launcher.ui.theme.IosColors

@Composable
fun AppContextMenu(
    app: AppItem?,
    isInDock: Boolean,
    onDismiss: () -> Unit,
    onEnterEditMode: () -> Unit,
    onOpenAppInfo: (AppItem) -> Unit,
    onToggleDock: (AppItem) -> Unit,
    onUninstallApp: (AppItem) -> Unit,
    onGroupIntoFolder: ((AppItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (app == null) return

    val isDark = isSystemInDarkTheme()
    val backdropColor = Color.Black.copy(alpha = 0.5f)
    val cardBg = if (isDark) Color(0xF02C2C2E) else Color(0xF0F2F2F7)
    val cardBorder = if (isDark) Color(0x33FFFFFF) else Color(0x26000000)
    val textColor = if (isDark) Color.White else Color.Black
    val dividerColor = if (isDark) Color(0x26FFFFFF) else Color(0x1F000000)

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.9f, animationSpec = tween(200)),
        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.9f, animationSpec = tween(160)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(backdropColor)
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                }
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(260.dp)
                    .shadow(20.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(0.35f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(0.5.dp, cardBorder, RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { /* consume tap inside */ }
                    }
            ) {
                // Header: App Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    SquircleIconView(
                        app = app,
                        iconSize = 38.dp,
                        showLabel = false,
                        onClick = { }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = app.label,
                            color = textColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (app.isSystemApp) "System App" else "Application",
                            color = if (isDark) Color(0x80FFFFFF) else Color(0x80000000),
                            fontSize = 12.sp
                        )
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                // Action: Edit Home Screen
                MenuItem(
                    icon = Icons.Rounded.Edit,
                    label = "Edit Home Screen",
                    onClick = {
                        onDismiss()
                        onEnterEditMode()
                    },
                    textColor = textColor
                )

                // Action: Group into Folder
                if (onGroupIntoFolder != null) {
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                    MenuItem(
                        icon = Icons.Rounded.Folder,
                        label = "Group into Folder…",
                        onClick = {
                            onDismiss()
                            onGroupIntoFolder(app)
                        },
                        textColor = textColor
                    )
                }

                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                // Action: Add / Remove from Dock
                MenuItem(
                    icon = Icons.Rounded.Dock,
                    label = if (isInDock) "Remove from Dock" else "Add to Dock",
                    onClick = {
                        onDismiss()
                        onToggleDock(app)
                    },
                    textColor = textColor
                )

                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                // Action: App Info
                MenuItem(
                    icon = Icons.Rounded.Info,
                    label = "App Info",
                    onClick = {
                        onDismiss()
                        onOpenAppInfo(app)
                    },
                    textColor = textColor
                )

                // Action: Delete / Uninstall (if non-system)
                if (!app.isSystemApp) {
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                    MenuItem(
                        icon = Icons.Rounded.Delete,
                        label = "Uninstall App",
                        onClick = {
                            onDismiss()
                            onUninstallApp(app)
                        },
                        textColor = IosColors.Red,
                        iconTint = IosColors.Red
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    textColor: Color,
    iconTint: Color = IosColors.Blue
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
    }
}
