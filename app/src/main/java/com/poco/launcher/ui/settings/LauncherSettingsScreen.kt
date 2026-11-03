package com.poco.launcher.ui.settings

import android.app.WallpaperManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poco.launcher.PocoLauncherApp
import com.poco.launcher.core.model.LauncherSettings
import com.poco.launcher.ui.theme.IosColors
import kotlinx.coroutines.launch

@Composable
fun LauncherSettingsScreen(
    isVisible: Boolean,
    settings: LauncherSettings,
    onUpdateSettings: (LauncherSettings) -> Unit,
    onSetAsDefault: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dataStore = remember { PocoLauncherApp.instance.launcherDataStore }

    val isDark = when (settings.themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val pageBg = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)
    val textPrimary = if (isDark) Color.White else Color.Black
    val textSecondary = if (isDark) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val dividerColor = if (isDark) Color(0x26FFFFFF) else Color(0x1F000000)

    var showExportDialog by remember { mutableStateOf(false) }
    var exportJsonString by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(220)) + slideInVertically(initialOffsetY = { it }, animationSpec = tween(280)),
        exit = fadeOut(tween(180)) + slideOutVertically(targetOffsetY = { it }, animationSpec = tween(240)),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Launcher Settings",
                    color = textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                // "Done" button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(IosColors.Blue.copy(alpha = 0.15f))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Done",
                        color = IosColors.Blue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                // SECTION 1: APPEARANCE
                SettingsGroup(title = "APPEARANCE", textSecondary = textSecondary, cardBg = cardBg, cardBorder = cardBorder) {
                    // Theme Mode Selector
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Color Theme",
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val themes = listOf("system" to "System", "light" to "Light", "dark" to "Dark")
                        IosSegmentedControl(
                            options = themes.map { it.second },
                            selectedIndex = themes.indexOfFirst { it.first == settings.themeMode }.coerceAtLeast(0),
                            onSelect = { idx ->
                                onUpdateSettings(settings.copy(themeMode = themes[idx].first))
                            },
                            isDark = isDark
                        )
                    }

                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    // Grid Size Selector
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Home Grid Layout",
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val gridOptions = listOf((4 to 5), (4 to 6), (5 to 6))
                        val gridLabels = listOf("4 × 5", "4 × 6", "5 × 6")
                        val currentGridIdx = gridOptions.indexOfFirst {
                            it.first == settings.gridColumns && it.second == settings.gridRows
                        }.coerceAtLeast(1)

                        IosSegmentedControl(
                            options = gridLabels,
                            selectedIndex = currentGridIdx,
                            onSelect = { idx ->
                                val (cols, rows) = gridOptions[idx]
                                onUpdateSettings(settings.copy(gridColumns = cols, gridRows = rows))
                            },
                            isDark = isDark
                        )
                    }

                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    // Icon Size Slider
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "App Icon Size",
                                color = textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${settings.iconSizeDp} dp",
                                color = IosColors.Blue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Slider(
                            value = settings.iconSizeDp.toFloat(),
                            onValueChange = { onUpdateSettings(settings.copy(iconSizeDp = it.toInt())) },
                            valueRange = 50f..72f,
                            steps = 11,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = IosColors.Blue,
                                inactiveTrackColor = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)
                            )
                        )
                    }

                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    // Show App Labels Switch
                    SettingsSwitchRow(
                        label = "Show App Labels",
                        subtitle = "Display app names beneath home icons",
                        checked = settings.showAppLabels,
                        onCheckedChange = { onUpdateSettings(settings.copy(showAppLabels = it)) },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )

                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    // Wallpaper Picker Action
                    SettingsActionRow(
                        label = "Change Wallpaper",
                        detail = "System Wallpaper",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SET_WALLPAPER).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Choose Wallpaper"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Wallpaper chooser unavailable", Toast.LENGTH_SHORT).show()
                            }
                        },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 2: GESTURES & CONTROLS
                SettingsGroup(title = "GESTURES & SYSTEM UI", textSecondary = textSecondary, cardBg = cardBg, cardBorder = cardBorder) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Swipe Down on Home Screen",
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val actions = listOf("spotlight" to "Spotlight Search", "control_center" to "Control Center")
                        val currentActionIdx = actions.indexOfFirst { it.first == settings.swipeDownAction }.coerceAtLeast(0)
                        IosSegmentedControl(
                            options = actions.map { it.second },
                            selectedIndex = currentActionIdx,
                            onSelect = { idx ->
                                onUpdateSettings(settings.copy(swipeDownAction = actions[idx].first))
                            },
                            isDark = isDark
                        )
                    }

                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SettingsSwitchRow(
                        label = "Control Center Gesture",
                        subtitle = "Swipe down from top right to open Control Center",
                        checked = settings.controlCenterEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(controlCenterEnabled = it)) },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 3: PERFORMANCE (POCO OPTIMIZATION)
                SettingsGroup(title = "POCO PERFORMANCE & BATTERY", textSecondary = textSecondary, cardBg = cardBg, cardBorder = cardBorder) {
                    SettingsSwitchRow(
                        label = "Blur & Translucent Surfaces",
                        subtitle = "Disable on budget 3GB RAM devices for maximum 120Hz smoothness",
                        checked = settings.blurEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(blurEnabled = it)) },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )

                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SettingsSwitchRow(
                        label = "Reduce Motion",
                        subtitle = "Minimizes animations to improve responsiveness",
                        checked = settings.reduceMotion,
                        onCheckedChange = { onUpdateSettings(settings.copy(reduceMotion = it)) },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 4: BACKUP & RESTORE
                SettingsGroup(title = "CONFIGURATION BACKUP", textSecondary = textSecondary, cardBg = cardBg, cardBorder = cardBorder) {
                    SettingsActionRow(
                        label = "Export Launcher Setup",
                        detail = "JSON Config",
                        onClick = {
                            coroutineScope.launch {
                                exportJsonString = dataStore.exportConfiguration()
                                showExportDialog = true
                            }
                        },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )

                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SettingsActionRow(
                        label = "Import Launcher Setup",
                        detail = "Restore from JSON",
                        onClick = {
                            importJsonInput = ""
                            showImportDialog = true
                        },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 5: SYSTEM DEFAULT & ABOUT
                SettingsGroup(title = "SYSTEM INTEGRATION", textSecondary = textSecondary, cardBg = cardBg, cardBorder = cardBorder) {
                    SettingsActionRow(
                        label = "Set as Default Launcher",
                        detail = "Home App",
                        onClick = onSetAsDefault,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )

                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "POCO Launcher Version",
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "1.0.0 (Release)",
                            color = textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(text = "Exported Setup", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Your layout, folders, and preferences have been exported to JSON:",
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Color(0x1F767680), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = exportJsonString,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("POCO Launcher Config", exportJsonString)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IosColors.Blue)
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(text = "Import Setup", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Paste your exported JSON configuration below:",
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        placeholder = { Text("Paste JSON here...", fontSize = 12.sp) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonInput.isNotBlank()) {
                            coroutineScope.launch {
                                val success = dataStore.importConfiguration(importJsonInput)
                                if (success) {
                                    Toast.makeText(context, "Setup restored successfully!", Toast.LENGTH_SHORT).show()
                                    showImportDialog = false
                                } else {
                                    Toast.makeText(context, "Invalid JSON format", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IosColors.Blue)
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    textSecondary: Color,
    cardBg: Color = if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color(0xFFFFFFFF),
    cardBorder: Color = if (isSystemInDarkTheme()) Color(0x33FFFFFF) else Color(0x1F000000),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(0.5.dp, cardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = textSecondary,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = IosColors.Green,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0x33767680)
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    label: String,
    detail: String? = null,
    onClick: () -> Unit,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            color = textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (detail != null) {
                Text(
                    text = detail,
                    color = textSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun IosSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    isDark: Boolean
) {
    val containerBg = if (isDark) Color(0x4D767680) else Color(0x1F767680)
    val selectedBg = if (isDark) Color(0xFF636366) else Color.White
    val selectedText = if (isDark) Color.White else Color.Black
    val unselectedText = if (isDark) Color(0x99FFFFFF) else Color(0x99000000)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(containerBg)
            .padding(2.dp)
    ) {
        options.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (isSelected) selectedBg else Color.Transparent)
                    .clickable { onSelect(index) }
            ) {
                Text(
                    text = title,
                    color = if (isSelected) selectedText else unselectedText,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
