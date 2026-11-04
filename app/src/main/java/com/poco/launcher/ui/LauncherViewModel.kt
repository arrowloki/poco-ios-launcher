package com.poco.launcher.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poco.launcher.PocoLauncherApp
import com.poco.launcher.core.model.AppItem
import com.poco.launcher.core.model.FolderItem
import com.poco.launcher.core.model.GridItem
import com.poco.launcher.core.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class LauncherViewModel : ViewModel() {

    private val appRepo = PocoLauncherApp.instance.appRepository
    private val dataStore = PocoLauncherApp.instance.launcherDataStore

    val installedApps: StateFlow<List<AppItem>> = appRepo.installedApps

    val settings: StateFlow<LauncherSettings> = dataStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = LauncherSettings()
    )

    val folders: StateFlow<List<FolderItem>> = dataStore.foldersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val widgetIds: StateFlow<List<Int>> = dataStore.widgetIdsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // UI Interactive States
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _selectedContextMenuApp = MutableStateFlow<AppItem?>(null)
    val selectedContextMenuApp: StateFlow<AppItem?> = _selectedContextMenuApp.asStateFlow()

    private val _activeOpenFolder = MutableStateFlow<FolderItem?>(null)
    val activeOpenFolder: StateFlow<FolderItem?> = _activeOpenFolder.asStateFlow()

    private val _isSpotlightVisible = MutableStateFlow(false)
    val isSpotlightVisible: StateFlow<Boolean> = _isSpotlightVisible.asStateFlow()

    private val _isSettingsVisible = MutableStateFlow(false)
    val isSettingsVisible: StateFlow<Boolean> = _isSettingsVisible.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(true)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    private val _isDefaultBannerDismissed = MutableStateFlow(false)
    val isDefaultBannerDismissed: StateFlow<Boolean> = _isDefaultBannerDismissed.asStateFlow()

    // iOS Volume HUD State
    private val _volumeHudRatio = MutableStateFlow<Float?>(null)
    val volumeHudRatio: StateFlow<Float?> = _volumeHudRatio.asStateFlow()
    private var volumeHudDismissJob: kotlinx.coroutines.Job? = null

    // App Grouping into Folder Dialog State
    private val _groupSourceApp = MutableStateFlow<AppItem?>(null)
    val groupSourceApp: StateFlow<AppItem?> = _groupSourceApp.asStateFlow()

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

    // Grid items combining widgets, folders and loose apps (excluding dock items)
    val gridItems: StateFlow<List<GridItem>> = combine(
        installedApps,
        dockApps,
        folders,
        widgetIds
    ) { apps, dock, currentFolders, widgets ->
        if (apps.isEmpty()) return@combine emptyList()

        val dockIds = dock.map { it.id }.toSet()
        val appMap = apps.associateBy { it.id }

        // Widgets
        val widgetGridItems = widgets.map { GridItem.Widget(it) }

        // Set of app IDs that belong to folders
        val folderAppIds = mutableSetOf<String>()
        val folderGridItems = mutableListOf<GridItem.Folder>()

        for (folder in currentFolders) {
            val childApps = folder.appIds.mapNotNull { appMap[it] }
            if (childApps.isNotEmpty()) {
                folderAppIds.addAll(folder.appIds)
                folderGridItems.add(GridItem.Folder(folder = folder, apps = childApps))
            }
        }

        // Loose apps on the home grid: not in dock and not inside any folder
        val looseAppItems = apps
            .filterNot { it.id in dockIds || it.id in folderAppIds }
            .map { GridItem.App(it) }

        // Widgets first, followed by folders, followed by apps
        widgetGridItems + folderGridItems + looseAppItems
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

    // App Execution
    fun launchApp(app: AppItem): Boolean {
        return appRepo.launchApp(app)
    }

    // Edit Mode Controls
    fun enterEditMode() {
        _isEditMode.value = true
        _selectedContextMenuApp.value = null
    }

    fun exitEditMode() {
        _isEditMode.value = false
    }

    // Context Menu Controls
    fun showContextMenu(app: AppItem) {
        if (!_isEditMode.value) {
            _selectedContextMenuApp.value = app
        }
    }

    fun dismissContextMenu() {
        _selectedContextMenuApp.value = null
    }

    // Folder View Controls
    fun openFolder(folder: FolderItem) {
        _activeOpenFolder.value = folder
    }

    fun closeFolder() {
        _activeOpenFolder.value = null
    }

    // Spotlight Controls
    fun showSpotlight() {
        _isSpotlightVisible.value = true
    }

    fun dismissSpotlight() {
        _isSpotlightVisible.value = false
    }

    // Settings Screen Controls
    fun openSettings() {
        _isSettingsVisible.value = true
        _selectedContextMenuApp.value = null
    }

    fun closeSettings() {
        _isSettingsVisible.value = false
    }

    // iOS Volume HUD Controls
    fun adjustVolume(delta: Int) {
        val sys = PocoLauncherApp.instance.systemControllers
        val ratio = sys.adjustVolumeBy(delta)
        _volumeHudRatio.value = ratio
        scheduleVolumeHudDismiss()
    }

    fun setVolumeRatio(ratio: Float) {
        val sys = PocoLauncherApp.instance.systemControllers
        val newVol = (ratio * sys.maxMusicVolume).toInt()
        sys.setMusicVolume(newVol)
        _volumeHudRatio.value = ratio
        scheduleVolumeHudDismiss()
    }

    private fun scheduleVolumeHudDismiss() {
        volumeHudDismissJob?.cancel()
        volumeHudDismissJob = viewModelScope.launch {
            kotlinx.coroutines.delay(2000L)
            _volumeHudRatio.value = null
        }
    }

    // App Grouping into Folder Controls
    fun openGroupDialog(app: AppItem) {
        _groupSourceApp.value = app
        _selectedContextMenuApp.value = null
    }

    fun closeGroupDialog() {
        _groupSourceApp.value = null
    }

    fun groupAppsIntoNewFolder(sourceApp: AppItem, targetApp: AppItem) {
        val folderTitle = if (sourceApp.label.length <= targetApp.label.length) sourceApp.label else targetApp.label
        createFolder(folderTitle, listOf(sourceApp.id, targetApp.id))
        closeGroupDialog()
    }

    fun addSourceAppToExistingFolder(sourceApp: AppItem, folder: FolderItem) {
        addAppToFolder(folder.id, sourceApp.id)
        closeGroupDialog()
    }

    // Folder Management
    fun createFolder(title: String, appIds: List<String>) {
        val current = folders.value.toMutableList()
        val newFolder = FolderItem(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Folder" },
            appIds = appIds
        )
        current.add(newFolder)
        viewModelScope.launch {
            dataStore.saveFolders(current)
        }
    }

    fun renameFolder(folderId: String, newTitle: String) {
        val updated = folders.value.map {
            if (it.id == folderId) it.copy(title = newTitle.ifBlank { "Folder" }) else it
        }
        viewModelScope.launch {
            dataStore.saveFolders(updated)
            if (_activeOpenFolder.value?.id == folderId) {
                _activeOpenFolder.value = _activeOpenFolder.value?.copy(title = newTitle.ifBlank { "Folder" })
            }
        }
    }

    fun addAppToFolder(folderId: String, appId: String) {
        val updated = folders.value.map { folder ->
            if (folder.id == folderId && !folder.appIds.contains(appId)) {
                folder.copy(appIds = folder.appIds + appId)
            } else folder
        }
        viewModelScope.launch {
            dataStore.saveFolders(updated)
        }
    }

    fun removeAppFromFolder(folderId: String, appId: String) {
        val updated = folders.value.mapNotNull { folder ->
            if (folder.id == folderId) {
                val remaining = folder.appIds.filterNot { it == appId }
                if (remaining.isEmpty()) null else folder.copy(appIds = remaining)
            } else folder
        }
        viewModelScope.launch {
            dataStore.saveFolders(updated)
            if (_activeOpenFolder.value?.id == folderId) {
                val remaining = _activeOpenFolder.value?.appIds?.filterNot { it == appId } ?: emptyList()
                if (remaining.isEmpty()) {
                    _activeOpenFolder.value = null
                } else {
                    _activeOpenFolder.value = _activeOpenFolder.value?.copy(appIds = remaining)
                }
            }
        }
    }

    fun deleteFolder(folderId: String) {
        val updated = folders.value.filterNot { it.id == folderId }
        viewModelScope.launch {
            dataStore.saveFolders(updated)
            if (_activeOpenFolder.value?.id == folderId) {
                _activeOpenFolder.value = null
            }
        }
    }

    // Widget Management
    fun addWidget(widgetId: Int) {
        viewModelScope.launch {
            dataStore.addWidgetId(widgetId)
        }
    }

    fun removeWidget(widgetId: Int) {
        viewModelScope.launch {
            dataStore.removeWidgetId(widgetId)
        }
    }

    // Dock Management
    fun toggleDockApp(app: AppItem) {
        val currentDock = dockApps.value.toMutableList()
        val exists = currentDock.any { it.id == app.id }
        if (exists) {
            // Keep at least 1 app in dock
            if (currentDock.size > 1) {
                currentDock.removeAll { it.id == app.id }
                setDockApps(currentDock)
            }
        } else {
            // Maximum 5 dock apps
            if (currentDock.size < 5) {
                currentDock.add(app)
                setDockApps(currentDock)
            }
        }
    }

    fun setDockApps(newDockApps: List<AppItem>) {
        viewModelScope.launch {
            dataStore.saveDockAppIds(newDockApps.map { it.id })
        }
    }

    // System Navigation / Settings deep links
    fun openAppInfo(context: Context, app: AppItem) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", app.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to manage applications
            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun uninstallApp(context: Context, app: AppItem) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", app.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to app info
            openAppInfo(context, app)
        }
    }

    fun updateSettings(newSettings: LauncherSettings) {
        viewModelScope.launch {
            dataStore.updateSettings(newSettings)
        }
    }

    fun checkDefaultLauncher(context: Context) {
        _isDefaultLauncher.value = com.poco.launcher.core.system.DefaultLauncherHelper.isDefaultLauncher(context)
    }

    fun dismissDefaultBanner() {
        _isDefaultBannerDismissed.value = true
    }

    fun promptSetAsDefault(context: Context) {
        val roleIntent = com.poco.launcher.core.system.DefaultLauncherHelper.createSetDefaultLauncherIntent(context)
        if (roleIntent != null) {
            try {
                if (context !is android.app.Activity) {
                    roleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(roleIntent)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        com.poco.launcher.core.system.DefaultLauncherHelper.openDefaultLauncherSettings(context)
    }
}
