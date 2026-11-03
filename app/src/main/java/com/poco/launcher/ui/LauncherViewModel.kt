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

    // UI Interactive States
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _selectedContextMenuApp = MutableStateFlow<AppItem?>(null)
    val selectedContextMenuApp: StateFlow<AppItem?> = _selectedContextMenuApp.asStateFlow()

    private val _activeOpenFolder = MutableStateFlow<FolderItem?>(null)
    val activeOpenFolder: StateFlow<FolderItem?> = _activeOpenFolder.asStateFlow()

    private val _isSpotlightVisible = MutableStateFlow(false)
    val isSpotlightVisible: StateFlow<Boolean> = _isSpotlightVisible.asStateFlow()

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

    // Grid items combining folders and loose apps (excluding dock items)
    val gridItems: StateFlow<List<GridItem>> = combine(
        installedApps,
        dockApps,
        folders
    ) { apps, dock, currentFolders ->
        if (apps.isEmpty()) return@combine emptyList()

        val dockIds = dock.map { it.id }.toSet()
        val appMap = apps.associateBy { it.id }

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

        // Folders first, followed by apps
        folderGridItems + looseAppItems
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
