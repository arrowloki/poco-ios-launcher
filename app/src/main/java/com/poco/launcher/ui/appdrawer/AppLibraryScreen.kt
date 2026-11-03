package com.poco.launcher.ui.appdrawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poco.launcher.core.model.AppItem
import com.poco.launcher.ui.components.SquircleIconView
import com.poco.launcher.ui.theme.IosColors
import kotlinx.coroutines.launch

@Composable
fun AppLibraryScreen(
    apps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xF2000000) else Color(0xF2F2F2F7)
    val textColor = if (isDark) Color.White else Color.Black
    val searchBg = if (isDark) Color(0x33767680) else Color(0x1F767680)

    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Filtered apps based on search
    val filteredApps by remember(searchQuery, apps) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                apps
            } else {
                val q = searchQuery.trim().lowercase()
                apps.filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
            }
        }
    }

    // Group apps by initial letter for A-Z indexing
    val groupedApps by remember(filteredApps) {
        derivedStateOf {
            filteredApps.groupBy {
                val firstChar = it.label.firstOrNull()?.uppercaseChar() ?: '#'
                if (firstChar.isLetter()) firstChar else '#'
            }.toSortedMap()
        }
    }

    val alphabet = remember { listOf('#') + ('A'..'Z').toList() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Title and Search Field
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "App Library",
                    color = textColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // iOS-Style Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search Apps",
                            color = if (isDark) Color(0x99FFFFFF) else Color(0x993C3C43),
                            fontSize = 15.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = if (isDark) Color(0x99FFFFFF) else Color(0x993C3C43),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Clear search",
                                    tint = if (isDark) Color.White else Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = searchBg,
                        unfocusedContainerColor = searchBg,
                        disabledContainerColor = searchBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // List of Apps with Letter Sections
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 32.dp, bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    groupedApps.forEach { (char, charApps) ->
                        item(key = "header_$char") {
                            Text(
                                text = char.toString(),
                                color = if (isDark) IosColors.Blue else IosColors.Blue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }

                        items(
                            items = charApps,
                            key = { it.id }
                        ) { app ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onAppClick(app) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                            ) {
                                SquircleIconView(
                                    app = app,
                                    iconSize = 46.dp,
                                    showLabel = false,
                                    onClick = { onAppClick(app) }
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = app.label,
                                        color = textColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = app.packageName,
                                        color = if (isDark) Color(0x80FFFFFF) else Color(0x80000000),
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Alphabet Index Scroller on the right
                if (searchQuery.isBlank()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp, top = 16.dp, bottom = 80.dp)
                    ) {
                        alphabet.forEach { letter ->
                            Text(
                                text = letter.toString(),
                                color = IosColors.Blue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(vertical = 1.dp)
                                    .clickable {
                                        val targetIndex = groupedApps.keys.indexOf(letter)
                                        if (targetIndex >= 0) {
                                            var scrollIndex = 0
                                            var i = 0
                                            for ((k, v) in groupedApps) {
                                                if (i == targetIndex) break
                                                scrollIndex += 1 + v.size
                                                i++
                                            }
                                            scope.launch {
                                                listState.animateScrollToItem(scrollIndex)
                                            }
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
