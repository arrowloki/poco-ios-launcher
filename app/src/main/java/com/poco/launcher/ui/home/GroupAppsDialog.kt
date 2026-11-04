package com.poco.launcher.ui.home

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poco.launcher.core.model.AppItem
import com.poco.launcher.core.model.FolderItem
import com.poco.launcher.ui.components.SquircleIconView
import com.poco.launcher.ui.theme.IosColors

@Composable
fun GroupAppsDialog(
    sourceApp: AppItem?,
    allApps: List<AppItem>,
    existingFolders: List<FolderItem>,
    onGroupWithApp: (targetApp: AppItem) -> Unit,
    onAddToFolder: (folder: FolderItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (sourceApp == null) return

    val isDark = isSystemInDarkTheme()
    val backdropColor = Color.Black.copy(alpha = 0.65f)
    val cardBg = if (isDark) Color(0xF21C1C1E) else Color(0xF2FFFFFF)
    val cardBorder = if (isDark) Color(0x33FFFFFF) else Color(0x26000000)
    val textPrimary = if (isDark) Color.White else Color.Black
    val textSecondary = if (isDark) Color(0xFF8E8E93) else Color(0xFF6C6C70)

    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(searchQuery, allApps, sourceApp.id) {
        val candidates = allApps.filterNot { it.id == sourceApp.id }
        if (searchQuery.isBlank()) candidates
        else {
            val q = searchQuery.trim().lowercase()
            candidates.filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
        }
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.9f, animationSpec = tween(220)),
        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.9f, animationSpec = tween(180)),
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
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .shadow(24.dp, RoundedCornerShape(26.dp), spotColor = Color.Black.copy(0.4f))
                    .clip(RoundedCornerShape(26.dp))
                    .background(cardBg)
                    .border(0.6.dp, cardBorder, RoundedCornerShape(26.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { /* consume tap inside */ }
                    }
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Group \"${sourceApp.label}\"",
                            color = textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Choose an app or folder to combine",
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x1F767680))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Existing Folders (if any)
                if (existingFolders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "ADD TO EXISTING FOLDER",
                        color = textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(existingFolders, key = { it.id }) { folder ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(IosColors.Blue.copy(alpha = 0.15f))
                                    .clickable {
                                        onAddToFolder(folder)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Folder,
                                    contentDescription = null,
                                    tint = IosColors.Blue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = folder.title,
                                    color = IosColors.Blue,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search field to find target app
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search app to group with…", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x1F767680),
                        unfocusedContainerColor = Color(0x1F767680),
                        disabledContainerColor = Color(0x1F767680),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                )

                Spacer(modifier = Modifier.height(10.dp))

                // List of apps to create a new folder with
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(filteredApps, key = { it.id }) { app ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onGroupWithApp(app)
                                    onDismiss()
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            SquircleIconView(
                                app = app,
                                iconSize = 38.dp,
                                showLabel = false,
                                onClick = { }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.label,
                                    color = textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Tap to create folder",
                                    color = IosColors.Blue,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                        HorizontalDivider(
                            color = if (isDark) Color(0x1AFFFFFF) else Color(0x14000000),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}
