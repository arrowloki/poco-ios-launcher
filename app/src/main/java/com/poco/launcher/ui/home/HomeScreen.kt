package com.poco.launcher.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poco.launcher.core.model.AppItem
import com.poco.launcher.core.model.FolderItem
import com.poco.launcher.core.model.GridItem
import com.poco.launcher.core.model.LauncherSettings
import com.poco.launcher.core.widget.LauncherWidgetHost
import com.poco.launcher.ui.appdrawer.AppLibraryScreen
import com.poco.launcher.ui.components.AppContextMenu
import com.poco.launcher.ui.components.DockBar
import com.poco.launcher.ui.components.PageIndicator
import com.poco.launcher.ui.components.SpotlightSearchPill
import com.poco.launcher.ui.components.SquircleFolderIconView
import com.poco.launcher.ui.components.SquircleIconView
import com.poco.launcher.ui.components.SquircleWidgetView
import com.poco.launcher.ui.controlcenter.ControlCenterSheet
import com.poco.launcher.ui.settings.LauncherSettingsScreen
import com.poco.launcher.ui.theme.IosColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    gridItems: List<GridItem>,
    dockApps: List<AppItem>,
    installedApps: List<AppItem>,
    settings: LauncherSettings,
    isEditMode: Boolean,
    selectedContextMenuApp: AppItem?,
    activeOpenFolder: FolderItem?,
    isSpotlightVisible: Boolean,
    isSettingsVisible: Boolean,
    widgetHost: LauncherWidgetHost? = null,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onFolderClick: (FolderItem) -> Unit,
    onEnterEditMode: () -> Unit,
    onExitEditMode: () -> Unit,
    onDismissContextMenu: () -> Unit,
    onCloseFolder: () -> Unit,
    onRenameFolder: (folderId: String, newTitle: String) -> Unit,
    onToggleDock: (AppItem) -> Unit,
    onOpenAppInfo: (AppItem) -> Unit,
    onUninstallApp: (AppItem) -> Unit,
    onShowSpotlight: () -> Unit,
    onDismissSpotlight: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onUpdateSettings: (LauncherSettings) -> Unit,
    onSetAsDefault: () -> Unit,
    onDeleteFolder: (folderId: String) -> Unit,
    onPickWidget: (() -> Unit)? = null,
    onRemoveWidget: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val itemsPerPage = settings.gridColumns * settings.gridRows

    // Partition items across home screen pages
    val pages = remember(gridItems, itemsPerPage) {
        if (gridItems.isEmpty()) listOf(emptyList())
        else gridItems.chunked(itemsPerPage.coerceAtLeast(1))
    }

    // Total pager count = home pages + 1 (App Library page at the end)
    val totalPagerCount = pages.size + 1
    val pagerState = rememberPagerState(pageCount = { totalPagerCount })

    var showControlCenter by remember { mutableStateOf(false) }

    val screenWidthPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Vertical Drag Detector: distinguishes top-right Control Center pull from downward swipe
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        val startX = change.position.x
                        val startY = change.position.y
                        if (settings.controlCenterEnabled && startY < 380f && startX > screenWidthPx * 0.60f && dragAmount > 20f) {
                            // Top right swipe: Control Center
                            change.consume()
                            showControlCenter = true
                        } else if (startY > 200f && dragAmount > 28f) {
                            // Home area swipe down
                            change.consume()
                            if (settings.swipeDownAction == "control_center" && settings.controlCenterEnabled) {
                                showControlCenter = true
                            } else {
                                onShowSpotlight()
                            }
                        }
                    }
                )
            }
            .pointerInput(isEditMode) {
                detectTapGestures {
                    if (isEditMode) {
                        onExitEditMode()
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Edit Mode Header: "Done" pill & "+" / "Settings" buttons
            AnimatedVisibility(
                visible = isEditMode,
                enter = fadeIn(tween(180)) + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut(tween(140)) + slideOutVertically(targetOffsetY = { -it })
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // "+" Add Widget Button (iOS style)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .background(Color(0x66FFFFFF))
                                .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(17.dp))
                                .clickable { onPickWidget?.invoke() }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add Widget",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Settings Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .background(Color(0x66FFFFFF))
                                .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(17.dp))
                                .clickable { onOpenSettings() }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Launcher Settings",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // "Done" Pill
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(0.3f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xCCFFFFFF))
                            .clickable { onExitEditMode() }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Done",
                            color = IosColors.Blue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Main Paginated Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = !isEditMode,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    if (pageIndex < pages.size) {
                        // Regular Home Screen Page
                        val pageItems = pages[pageIndex]
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(settings.gridColumns),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            userScrollEnabled = false,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = pageItems,
                                key = { it.id },
                                span = { item ->
                                    if (item is GridItem.Widget) GridItemSpan(maxLineSpan) else GridItemSpan(1)
                                }
                            ) { gridItem ->
                                when (gridItem) {
                                    is GridItem.Widget -> {
                                        if (widgetHost != null) {
                                            SquircleWidgetView(
                                                widgetId = gridItem.widgetId,
                                                widgetHost = widgetHost,
                                                isEditMode = isEditMode,
                                                onRemoveClick = { onRemoveWidget?.invoke(gridItem.widgetId) }
                                            )
                                        }
                                    }
                                    is GridItem.App -> {
                                        SquircleIconView(
                                            app = gridItem.app,
                                            iconSize = settings.iconSizeDp.dp,
                                            showLabel = settings.showAppLabels,
                                            isEditMode = isEditMode,
                                            onRemoveClick = { onUninstallApp(gridItem.app) },
                                            onClick = { onAppClick(gridItem.app) },
                                            onLongClick = { onAppLongClick(gridItem.app) }
                                        )
                                    }
                                    is GridItem.Folder -> {
                                        SquircleFolderIconView(
                                            title = gridItem.title,
                                            apps = gridItem.apps,
                                            iconSize = settings.iconSizeDp.dp,
                                            showLabel = settings.showAppLabels,
                                            isEditMode = isEditMode,
                                            onRemoveClick = { onDeleteFolder(gridItem.folder.id) },
                                            onClick = { onFolderClick(gridItem.folder) },
                                            onLongClick = { onEnterEditMode() }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Last Page: Embedded iOS App Library
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
                    if (!isEditMode) {
                        // Spotlight Search Pill
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            SpotlightSearchPill(
                                onClick = onShowSpotlight
                            )
                        }

                        // Page Dots
                        PageIndicator(
                            pageCount = pages.size,
                            currentPage = pagerState.currentPage,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    // Translucent iOS Dock
                    DockBar(
                        dockApps = dockApps,
                        iconSize = (settings.iconSizeDp - 2).dp,
                        isEditMode = isEditMode,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        onRemoveClick = { onToggleDock(it) }
                    )
                }
            }
        }

        // Spotlight Search Overlay
        SpotlightSearchOverlay(
            isVisible = isSpotlightVisible,
            allApps = installedApps,
            onDismiss = onDismissSpotlight,
            onAppClick = onAppClick
        )

        // App Long-Press Context Menu
        if (selectedContextMenuApp != null) {
            val isInDock = dockApps.any { it.id == selectedContextMenuApp.id }
            AppContextMenu(
                app = selectedContextMenuApp,
                isInDock = isInDock,
                onDismiss = onDismissContextMenu,
                onEnterEditMode = onEnterEditMode,
                onOpenAppInfo = onOpenAppInfo,
                onToggleDock = onToggleDock,
                onUninstallApp = onUninstallApp
            )
        }

        // Folder Dialog Popup
        if (activeOpenFolder != null) {
            val appMap = remember(installedApps) { installedApps.associateBy { it.id } }
            val folderApps = remember(activeOpenFolder, appMap) {
                activeOpenFolder.appIds.mapNotNull { appMap[it] }
            }
            FolderDialog(
                folder = activeOpenFolder,
                apps = folderApps,
                onDismiss = onCloseFolder,
                onAppClick = onAppClick,
                onRenameFolder = onRenameFolder
            )
        }

        // iOS Control Center Overlay Sheet
        ControlCenterSheet(
            isVisible = showControlCenter,
            onDismiss = { showControlCenter = false },
            onOpenSettings = onOpenSettings
        )

        // Dedicated Launcher Settings Screen Overlay
        LauncherSettingsScreen(
            isVisible = isSettingsVisible,
            settings = settings,
            onUpdateSettings = onUpdateSettings,
            onSetAsDefault = onSetAsDefault,
            onDismiss = onCloseSettings
        )
    }
}
