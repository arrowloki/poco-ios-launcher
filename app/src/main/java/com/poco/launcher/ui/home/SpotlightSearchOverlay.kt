package com.poco.launcher.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poco.launcher.core.model.AppItem
import com.poco.launcher.ui.components.SquircleIconView
import com.poco.launcher.ui.theme.IosColors

@Composable
fun SpotlightSearchOverlay(
    isVisible: Boolean,
    allApps: List<AppItem>,
    onDismiss: () -> Unit,
    onAppClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val isDark = isSystemInDarkTheme()
    val overlayBg = if (isDark) Color(0xD9000000) else Color(0xB31C1C1E)
    val cardBg = if (isDark) Color(0x33767680) else Color(0x26FFFFFF)
    val cardBorder = if (isDark) Color(0x33FFFFFF) else Color(0x26000000)
    val textColor = Color.White

    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val filteredApps by remember(query, allApps) {
        derivedStateOf {
            if (query.isBlank()) emptyList()
            else {
                val q = query.trim().lowercase()
                allApps.filter {
                    it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
                }.take(12)
            }
        }
    }

    // Suggested apps (first 5 popular apps)
    val suggestedApps = remember(allApps) { allApps.take(5) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            focusRequester.requestFocus()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(180)) + slideInVertically(initialOffsetY = { -it / 4 }),
        exit = fadeOut(tween(160)) + slideOutVertically(targetOffsetY = { -it / 4 }),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayBg)
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                }
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures { /* consume tap inside */ }
                    }
            ) {
                // Search Input Box
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(
                            text = "Search Apps, Contacts & Web…",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 15.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(19.dp)
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        disabledContainerColor = cardBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(0.5.dp, cardBorder, RoundedCornerShape(16.dp))
                        .focusRequester(focusRequester)
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (query.isBlank()) {
                    // Suggestions Section
                    Text(
                        text = "SIRI SUGGESTIONS",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardBg)
                            .border(0.5.dp, cardBorder, RoundedCornerShape(20.dp))
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        suggestedApps.forEach { app ->
                            SquircleIconView(
                                app = app,
                                iconSize = 48.dp,
                                showLabel = true,
                                textColor = Color.White,
                                onClick = {
                                    onDismiss()
                                    onAppClick(app)
                                }
                            )
                        }
                    }
                } else {
                    // Live Search Results List
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardBg)
                            .border(0.5.dp, cardBorder, RoundedCornerShape(20.dp))
                    ) {
                        items(
                            items = filteredApps,
                            key = { it.id }
                        ) { app ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismiss()
                                        onAppClick(app)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                SquircleIconView(
                                    app = app,
                                    iconSize = 40.dp,
                                    showLabel = false,
                                    onClick = {
                                        onDismiss()
                                        onAppClick(app)
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = app.label,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Application",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
