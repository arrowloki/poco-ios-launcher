package com.poco.launcher.ui

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
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
                        installedApps = installedApps,
                        dockApps = dockApps,
                        settings = settings,
                        onAppClick = { app ->
                            viewModel.launchApp(app)
                        },
                        onAppLongClick = { app ->
                            // Long-press options / drag-drop placeholder
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
