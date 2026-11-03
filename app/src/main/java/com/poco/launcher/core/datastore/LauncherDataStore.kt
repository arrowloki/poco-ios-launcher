package com.poco.launcher.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.poco.launcher.core.model.FolderItem
import com.poco.launcher.core.model.LauncherSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "poco_launcher_prefs")

class LauncherDataStore(private val context: Context) {

    private object Keys {
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val GRID_ROWS = intPreferencesKey("grid_rows")
        val ICON_SIZE_DP = intPreferencesKey("icon_size_dp")
        val SHOW_APP_LABELS = booleanPreferencesKey("show_app_labels")
        val DOCK_APPS_COUNT = intPreferencesKey("dock_apps_count")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CONTROL_CENTER_ENABLED = booleanPreferencesKey("control_center_enabled")
        val BLUR_ENABLED = booleanPreferencesKey("blur_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val SWIPE_DOWN_ACTION = stringPreferencesKey("swipe_down_action")

        // CSV list of dock app IDs ("packageName/className")
        val DOCK_APP_IDS = stringPreferencesKey("dock_app_ids")
        // Stored page layouts in format: "id1,id2,id3|id4,id5,id6"
        val PAGES_LAYOUT = stringPreferencesKey("pages_layout")
        // Stored folders format: id:::title:::appId1,appId2;;;...
        val FOLDERS = stringPreferencesKey("launcher_folders")
        // Stored widget IDs
        val WIDGET_IDS = stringPreferencesKey("widget_ids")
    }

    val settingsFlow: Flow<LauncherSettings> = context.dataStore.data.map { prefs ->
        LauncherSettings(
            gridColumns = prefs[Keys.GRID_COLUMNS] ?: 4,
            gridRows = prefs[Keys.GRID_ROWS] ?: 6,
            iconSizeDp = prefs[Keys.ICON_SIZE_DP] ?: 60,
            showAppLabels = prefs[Keys.SHOW_APP_LABELS] ?: true,
            dockAppsCount = prefs[Keys.DOCK_APPS_COUNT] ?: 4,
            themeMode = prefs[Keys.THEME_MODE] ?: "system",
            controlCenterEnabled = prefs[Keys.CONTROL_CENTER_ENABLED] ?: true,
            blurEnabled = prefs[Keys.BLUR_ENABLED] ?: true,
            reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false,
            swipeDownAction = prefs[Keys.SWIPE_DOWN_ACTION] ?: "spotlight"
        )
    }

    val dockAppIdsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.DOCK_APP_IDS] ?: ""
        if (raw.isBlank()) emptyList() else raw.split(",").filter { it.isNotBlank() }
    }

    val pagesLayoutFlow: Flow<List<List<String>>> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.PAGES_LAYOUT] ?: ""
        if (raw.isBlank()) {
            emptyList()
        } else {
            raw.split("|").map { page ->
                page.split(",").filter { it.isNotBlank() }
            }
        }
    }

    val foldersFlow: Flow<List<FolderItem>> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.FOLDERS] ?: ""
        if (raw.isBlank()) {
            emptyList()
        } else {
            raw.split(";;;").mapNotNull { entry ->
                val parts = entry.split(":::")
                if (parts.size >= 3) {
                    val id = parts[0]
                    val title = parts[1]
                    val appIds = parts[2].split(",").filter { it.isNotBlank() }
                    FolderItem(id = id, title = title, appIds = appIds)
                } else null
            }
        }
    }

    val widgetIdsFlow: Flow<List<Int>> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.WIDGET_IDS] ?: ""
        if (raw.isBlank()) emptyList() else raw.split(",").mapNotNull { it.toIntOrNull() }
    }

    suspend fun updateSettings(settings: LauncherSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GRID_COLUMNS] = settings.gridColumns
            prefs[Keys.GRID_ROWS] = settings.gridRows
            prefs[Keys.ICON_SIZE_DP] = settings.iconSizeDp
            prefs[Keys.SHOW_APP_LABELS] = settings.showAppLabels
            prefs[Keys.DOCK_APPS_COUNT] = settings.dockAppsCount
            prefs[Keys.THEME_MODE] = settings.themeMode
            prefs[Keys.CONTROL_CENTER_ENABLED] = settings.controlCenterEnabled
            prefs[Keys.BLUR_ENABLED] = settings.blurEnabled
            prefs[Keys.REDUCE_MOTION] = settings.reduceMotion
            prefs[Keys.SWIPE_DOWN_ACTION] = settings.swipeDownAction
        }
    }

    suspend fun saveDockAppIds(ids: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DOCK_APP_IDS] = ids.joinToString(",")
        }
    }

    suspend fun savePagesLayout(pages: List<List<String>>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PAGES_LAYOUT] = pages.joinToString("|") { page ->
                page.joinToString(",")
            }
        }
    }

    suspend fun saveFolders(folders: List<FolderItem>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FOLDERS] = folders.joinToString(";;;") { folder ->
                "${folder.id}:::${folder.title}:::${folder.appIds.joinToString(",")}"
            }
        }
    }

    suspend fun saveWidgetIds(ids: List<Int>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WIDGET_IDS] = ids.joinToString(",")
        }
    }

    suspend fun addWidgetId(id: Int) {
        context.dataStore.edit { prefs ->
            val raw = prefs[Keys.WIDGET_IDS] ?: ""
            val current = if (raw.isBlank()) emptyList() else raw.split(",").mapNotNull { it.toIntOrNull() }
            if (!current.contains(id)) {
                prefs[Keys.WIDGET_IDS] = (current + id).joinToString(",")
            }
        }
    }

    suspend fun removeWidgetId(id: Int) {
        context.dataStore.edit { prefs ->
            val raw = prefs[Keys.WIDGET_IDS] ?: ""
            val current = if (raw.isBlank()) emptyList() else raw.split(",").mapNotNull { it.toIntOrNull() }
            prefs[Keys.WIDGET_IDS] = current.filterNot { it == id }.joinToString(",")
        }
    }

    // --- Configuration Backup & Restore ---
    suspend fun exportConfiguration(): String {
        val prefs = context.dataStore.data.first()
        val json = JSONObject()
        val settingsObj = JSONObject()
        settingsObj.put("gridColumns", prefs[Keys.GRID_COLUMNS] ?: 4)
        settingsObj.put("gridRows", prefs[Keys.GRID_ROWS] ?: 6)
        settingsObj.put("iconSizeDp", prefs[Keys.ICON_SIZE_DP] ?: 60)
        settingsObj.put("showAppLabels", prefs[Keys.SHOW_APP_LABELS] ?: true)
        settingsObj.put("dockAppsCount", prefs[Keys.DOCK_APPS_COUNT] ?: 4)
        settingsObj.put("themeMode", prefs[Keys.THEME_MODE] ?: "system")
        settingsObj.put("controlCenterEnabled", prefs[Keys.CONTROL_CENTER_ENABLED] ?: true)
        settingsObj.put("blurEnabled", prefs[Keys.BLUR_ENABLED] ?: true)
        settingsObj.put("reduceMotion", prefs[Keys.REDUCE_MOTION] ?: false)
        settingsObj.put("swipeDownAction", prefs[Keys.SWIPE_DOWN_ACTION] ?: "spotlight")
        json.put("settings", settingsObj)
        json.put("dockAppIds", prefs[Keys.DOCK_APP_IDS] ?: "")
        json.put("folders", prefs[Keys.FOLDERS] ?: "")
        json.put("widgetIds", prefs[Keys.WIDGET_IDS] ?: "")
        return json.toString(2)
    }

    suspend fun importConfiguration(rawJson: String): Boolean {
        return try {
            val json = JSONObject(rawJson)
            val settingsObj = json.getJSONObject("settings")
            context.dataStore.edit { prefs ->
                prefs[Keys.GRID_COLUMNS] = settingsObj.optInt("gridColumns", 4)
                prefs[Keys.GRID_ROWS] = settingsObj.optInt("gridRows", 6)
                prefs[Keys.ICON_SIZE_DP] = settingsObj.optInt("iconSizeDp", 60)
                prefs[Keys.SHOW_APP_LABELS] = settingsObj.optBoolean("showAppLabels", true)
                prefs[Keys.DOCK_APPS_COUNT] = settingsObj.optInt("dockAppsCount", 4)
                prefs[Keys.THEME_MODE] = settingsObj.optString("themeMode", "system")
                prefs[Keys.CONTROL_CENTER_ENABLED] = settingsObj.optBoolean("controlCenterEnabled", true)
                prefs[Keys.BLUR_ENABLED] = settingsObj.optBoolean("blurEnabled", true)
                prefs[Keys.REDUCE_MOTION] = settingsObj.optBoolean("reduceMotion", false)
                prefs[Keys.SWIPE_DOWN_ACTION] = settingsObj.optString("swipeDownAction", "spotlight")
                if (json.has("dockAppIds")) {
                    prefs[Keys.DOCK_APP_IDS] = json.getString("dockAppIds")
                }
                if (json.has("folders")) {
                    prefs[Keys.FOLDERS] = json.getString("folders")
                }
                if (json.has("widgetIds")) {
                    prefs[Keys.WIDGET_IDS] = json.getString("widgetIds")
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
