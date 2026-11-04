package com.poco.launcher.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.poco.launcher.core.system.DefaultLauncherHelper
import com.poco.launcher.core.widget.LauncherWidgetHost
import com.poco.launcher.ui.home.HomeScreen
import com.poco.launcher.ui.theme.PocoLauncherTheme

class LauncherActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    private lateinit var widgetHost: LauncherWidgetHost
    private lateinit var appWidgetManager: AppWidgetManager
    private var pendingWidgetId: Int = -1

    private val defaultRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkDefaultLauncher(this)
    }

    private val widgetPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        if (result.resultCode == RESULT_OK) {
            if (appWidgetId != -1) {
                configureOrAddWidget(appWidgetId)
            }
        } else {
            if (appWidgetId != -1) {
                widgetHost.deleteAppWidgetId(appWidgetId)
            }
        }
    }

    private val widgetConfigureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidgetId) ?: pendingWidgetId
        if (result.resultCode == RESULT_OK) {
            if (appWidgetId != -1) {
                viewModel.addWidget(appWidgetId)
            }
        } else {
            if (appWidgetId != -1) {
                widgetHost.deleteAppWidgetId(appWidgetId)
            }
        }
        pendingWidgetId = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AppWidget host & manager
        widgetHost = LauncherWidgetHost(applicationContext)
        appWidgetManager = AppWidgetManager.getInstance(applicationContext)

        // Verify default launcher state
        viewModel.checkDefaultLauncher(this)

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
            val isSettingsVisible by viewModel.isSettingsVisible.collectAsState()
            val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsState()
            val isDefaultBannerDismissed by viewModel.isDefaultBannerDismissed.collectAsState()
            val volumeHudRatio by viewModel.volumeHudRatio.collectAsState()
            val groupSourceApp by viewModel.groupSourceApp.collectAsState()
            val folders by viewModel.folders.collectAsState()

            // Crucial: BackHandler is unconditionally enabled (enabled = true).
            // A home launcher activity must NEVER close or call finish() when Back is pressed!
            BackHandler(enabled = true) {
                when {
                    isSettingsVisible -> viewModel.closeSettings()
                    isSpotlightVisible -> viewModel.dismissSpotlight()
                    activeOpenFolder != null -> viewModel.closeFolder()
                    selectedContextMenuApp != null -> viewModel.dismissContextMenu()
                    isEditMode -> viewModel.exitEditMode()
                    else -> {
                        // At root home screen: consume event, never exit launcher!
                    }
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
                        isSettingsVisible = isSettingsVisible,
                        isDefaultLauncher = isDefaultLauncher,
                        isDefaultBannerDismissed = isDefaultBannerDismissed,
                        volumeHudRatio = volumeHudRatio,
                        groupSourceApp = groupSourceApp,
                        folders = folders,
                        widgetHost = widgetHost,
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
                        onOpenSettings = {
                            viewModel.openSettings()
                        },
                        onCloseSettings = {
                            viewModel.closeSettings()
                        },
                        onUpdateSettings = { newSettings ->
                            viewModel.updateSettings(newSettings)
                        },
                        onSetAsDefault = {
                            requestSetDefault()
                        },
                        onDismissDefaultBanner = {
                            viewModel.dismissDefaultBanner()
                        },
                        onDeleteFolder = { folderId ->
                            viewModel.deleteFolder(folderId)
                        },
                        onPickWidget = {
                            pickWidget()
                        },
                        onRemoveWidget = { widgetId ->
                            removeWidget(widgetId)
                        },
                        onVolumeChange = { ratio ->
                            viewModel.setVolumeRatio(ratio)
                        },
                        onOpenGroupDialog = { app ->
                            viewModel.openGroupDialog(app)
                        },
                        onCloseGroupDialog = {
                            viewModel.closeGroupDialog()
                        },
                        onGroupWithApp = { targetApp ->
                            val src = groupSourceApp
                            if (src != null) {
                                viewModel.groupAppsIntoNewFolder(src, targetApp)
                            }
                        },
                        onAddSourceToFolder = { folder ->
                            val src = groupSourceApp
                            if (src != null) {
                                viewModel.addSourceAppToExistingFolder(src, folder)
                            }
                        }
                    )
                }
            }
        }
    }

    private fun requestSetDefault() {
        val roleIntent = DefaultLauncherHelper.createSetDefaultLauncherIntent(this)
        if (roleIntent != null) {
            defaultRoleLauncher.launch(roleIntent)
        } else {
            DefaultLauncherHelper.openDefaultLauncherSettings(this)
        }
    }

    private fun pickWidget() {
        try {
            val appWidgetId = widgetHost.allocateAppWidgetId()
            val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            widgetPickerLauncher.launch(pickIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun configureOrAddWidget(appWidgetId: Int) {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
        if (info?.configure != null) {
            pendingWidgetId = appWidgetId
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = info.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            widgetConfigureLauncher.launch(intent)
        } else {
            viewModel.addWidget(appWidgetId)
        }
    }

    private fun removeWidget(appWidgetId: Int) {
        try {
            widgetHost.deleteAppWidgetId(appWidgetId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        viewModel.removeWidget(appWidgetId)
    }

    override fun onStart() {
        super.onStart()
        try {
            widgetHost.startListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            widgetHost.stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh battery and system states
        PocoLauncherApp.instance.systemControllers.updateBatteryState()
        // Refresh default launcher status
        viewModel.checkDefaultLauncher(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent default finish - Home launcher must stay active
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Handled via Compose BackHandler, prevent Activity from finishing
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            viewModel.adjustVolume(1)
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            viewModel.adjustVolume(-1)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}
