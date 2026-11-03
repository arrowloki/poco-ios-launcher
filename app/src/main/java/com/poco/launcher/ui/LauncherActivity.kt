package com.poco.launcher.ui

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.poco.launcher.PocoLauncherApp
import com.poco.launcher.ui.home.HomeScreen
import com.poco.launcher.ui.theme.PocoLauncherTheme

class LauncherActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Configure Edge-to-Edge full immersion layout
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Allow system wallpaper to shine through
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
        )

        // 3. Set transparent system status & navigation bars
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            val settings by viewModel.settings.collectAsState()
            val installedApps by viewModel.installedApps.collectAsState()
            val dockApps by viewModel.dockApps.collectAsState()
            val gridItems by viewModel.gridItems.collectAsState()
            val isEditMode by viewModel.isEditMode.collectAsState()
            val selectedContextMenuApp by viewModel.selectedContextMenuApp.collectAsState()
            val activeOpenFolder by viewModel.activeOpenFolder.collectAsState()
            val isSpotlightVisible by viewModel.isSpotlightVisible.collectAsState()

            // Back button handling: dismiss topmost modal layer first
            BackHandler(
                enabled = isSpotlightVisible || activeOpenFolder != null || selectedContextMenuApp != null || isEditMode
            ) {
                when {
                    isSpotlightVisible -> viewModel.dismissSpotlight()
                    activeOpenFolder != null -> viewModel.closeFolder()
                    selectedContextMenuApp != null -> viewModel.dismissContextMenu()
                    isEditMode -> viewModel.exitEditMode()
                }
            }

            val isDark = when (settings.themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            // Status bar icon appearance
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }

            PocoLauncherTheme(themeMode = settings.themeMode) {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxSize()
                ) {
                    HomeScreen(
                        gridItems = gridItems,
                        dockApps = dockApps,
                        installedApps = installedApps,
                        settings = settings,
                        isEditMode = isEditMode,
                        selectedContextMenuApp = selectedContextMenuApp,
                        activeOpenFolder = activeOpenFolder,
                        isSpotlightVisible = isSpotlightVisible,
                        onAppClick = { app ->
                            viewModel.launchApp(app)
                        },
                        onAppLongClick = { app ->
                            viewModel.showContextMenu(app)
                        },
                        onFolderClick = { folder ->
                            viewModel.openFolder(folder)
                        },
                        onEnterEditMode = {
                            viewModel.enterEditMode()
                        },
                        onExitEditMode = {
                            viewModel.exitEditMode()
                        },
                        onDismissContextMenu = {
                            viewModel.dismissContextMenu()
                        },
                        onCloseFolder = {
                            viewModel.closeFolder()
                        },
                        onRenameFolder = { folderId, newTitle ->
                            viewModel.renameFolder(folderId, newTitle)
                        },
                        onToggleDock = { app ->
                            viewModel.toggleDockApp(app)
                        },
                        onOpenAppInfo = { app ->
                            viewModel.openAppInfo(this@LauncherActivity, app)
                        },
                        onUninstallApp = { app ->
                            viewModel.uninstallApp(this@LauncherActivity, app)
                        },
                        onShowSpotlight = {
                            viewModel.showSpotlight()
                        },
                        onDismissSpotlight = {
                            viewModel.dismissSpotlight()
                        },
                        onDeleteFolder = { folderId ->
                            viewModel.deleteFolder(folderId)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh battery and system states
        PocoLauncherApp.instance.systemControllers.updateBatteryState()
    }
}
