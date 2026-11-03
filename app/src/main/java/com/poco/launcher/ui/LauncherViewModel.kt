package com.poco.launcher.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poco.launcher.PocoLauncherApp
import com.poco.launcher.core.model.AppItem
import com.poco.launcher.core.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel : ViewModel() {

    private val appRepo = PocoLauncherApp.instance.appRepository
    private val dataStore = PocoLauncherApp.instance.launcherDataStore

    val installedApps: StateFlow<List<AppItem>> = appRepo.installedApps

    val settings: StateFlow<LauncherSettings> = dataStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = LauncherSettings()
    )

    // Dock apps combined from stored IDs or intelligent system defaults
    val dockApps: StateFlow<List<AppItem>> = combine(
        installedApps,
        dataStore.dockAppIdsFlow
    ) { apps, storedDockIds ->
        if (apps.isEmpty()) return@combine emptyList()

        if (storedDockIds.isNotEmpty()) {
            val appMap = apps.associateBy { it.id }
            val resolved = storedDockIds.mapNotNull { appMap[it] }
            if (resolved.isNotEmpty()) return@combine resolved
        }

        // Intelligent defaults for POCO / Android: Phone, Messages, Browser, Camera
        selectDefaultDockApps(apps)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private fun selectDefaultDockApps(apps: List<AppItem>): List<AppItem> {
        val candidates = mutableListOf<AppItem>()

        // 1. Phone / Dialer
        apps.firstOrNull {
            it.packageName.contains("dialer") || it.packageName.contains("phone") ||
                    it.label.equals("Phone", ignoreCase = true)
        }?.let { candidates.add(it) }

        // 2. Messages / SMS
        apps.firstOrNull {
            it.packageName.contains("messaging") || it.packageName.contains("mms") ||
                    it.label.equals("Messages", ignoreCase = true)
        }?.let { candidates.add(it) }

        // 3. Browser
        apps.firstOrNull {
            it.packageName.contains("chrome") || it.packageName.contains("browser") ||
                    it.label.equals("Chrome", ignoreCase = true)
        }?.let { candidates.add(it) }

        // 4. Camera
        apps.firstOrNull {
            it.packageName.contains("camera") || it.label.equals("Camera", ignoreCase = true)
        }?.let { candidates.add(it) }

        // Fill remaining slots up to 4 if any were missing
        if (candidates.size < 4) {
            val usedIds = candidates.map { it.id }.toSet()
            val remaining = apps.filterNot { it.id in usedIds }.take(4 - candidates.size)
            candidates.addAll(remaining)
        }

        return candidates.take(4)
    }

    fun launchApp(app: AppItem): Boolean {
        return appRepo.launchApp(app)
    }

    fun setDockApps(newDockApps: List<AppItem>) {
        viewModelScope.launch {
            dataStore.saveDockAppIds(newDockApps.map { it.id })
        }
    }

    fun updateSettings(newSettings: LauncherSettings) {
        viewModelScope.launch {
            dataStore.updateSettings(newSettings)
        }
    }

    fun promptSetAsDefault(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return
                }
            }
        }

        // Fallback for devices / MIUI
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val homeIntent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(homeIntent)
        }
    }
}
