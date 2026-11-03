package com.poco.launcher.ui.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.poco.launcher.PocoLauncherApp
import com.poco.launcher.core.model.AppItem
import com.poco.launcher.core.model.LauncherSettings
import com.poco.launcher.ui.appdrawer.AppLibraryScreen
import com.poco.launcher.ui.components.DockBar
import com.poco.launcher.ui.components.PageIndicator
import com.poco.launcher.ui.components.SpotlightSearchPill
import com.poco.launcher.ui.components.SquircleIconView
import com.poco.launcher.ui.controlcenter.ControlCenterSheet

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    installedApps: List<AppItem>,
    dockApps: List<AppItem>,
    settings: LauncherSettings,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemsPerPage = settings.gridColumns * settings.gridRows

    // Exclude dock apps from standard home screen pages
    val gridApps = remember(installedApps, dockApps) {
        val dockIds = dockApps.map { it.id }.toSet()
        installedApps.filterNot { it.id in dockIds }
    }

    // Partition apps across pages
    val pages = remember(gridApps, itemsPerPage) {
        if (gridApps.isEmpty()) listOf(emptyList())
        else gridApps.chunked(itemsPerPage.coerceAtLeast(1))
    }

    // Total pager count = home pages + 1 (App Library page at the end)
    val totalPagerCount = pages.size + 1
    val pagerState = rememberPagerState(pageCount = { totalPagerCount })

    var showControlCenter by remember { mutableStateOf(false) }
    var showAppLibraryOverlay by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Top swipe-down detector for Control Center (swipe down from top right)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        // Detect swipe-down from upper half of screen
                        if (change.position.y < 350f && dragAmount > 20f) {
                            change.consume()
                            showControlCenter = true
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Main Paginated Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    if (pageIndex < pages.size) {
                        // Regular Home Screen Page
                        val pageApps = pages[pageIndex]
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(settings.gridColumns),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            userScrollEnabled = false, // Grid fills page, horizontal pager swipes
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = pageApps,
                                key = { it.id }
                            ) { app ->
                                SquircleIconView(
                                    app = app,
                                    iconSize = settings.iconSizeDp.dp,
                                    showLabel = settings.showAppLabels,
                                    onClick = { onAppClick(app) },
                                    onLongClick = { onAppLongClick(app) }
                                )
                            }
                        }
                    } else {
                        // Last Page: Embedded App Library
                        AppLibraryScreen(
                            apps = installedApps,
                            onAppClick = onAppClick
                        )
                    }
                }
            }

            // Bottom Section: Search Pill / Page Indicator & Dock (hidden when on App Library page)
            if (pagerState.currentPage < pages.size) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Spotlight Search Pill & Dots
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        SpotlightSearchPill(
                            onClick = { showAppLibraryOverlay = true }
                        )
                    }

                    PageIndicator(
                        pageCount = pages.size,
                        currentPage = pagerState.currentPage,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Translucent iOS Dock
                    DockBar(
                        dockApps = dockApps,
                        iconSize = (settings.iconSizeDp - 2).dp,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick
                    )
                }
            }
        }

        // App Library Overlay (when triggered via Spotlight search pill)
        AnimatedVisibility(
            visible = showAppLibraryOverlay,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(200))
        ) {
            AppLibraryScreen(
                apps = installedApps,
                onAppClick = {
                    showAppLibraryOverlay = false
                    onAppClick(it)
                }
            )
        }

        // iOS Control Center Overlay Sheet
        ControlCenterSheet(
            isVisible = showControlCenter,
            onDismiss = { showControlCenter = false }
        )
    }
}
