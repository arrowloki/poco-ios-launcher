package com.poco.launcher.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.poco.launcher.core.model.LauncherSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

        // JSON/CSV list of dock app IDs ("packageName/className")
        val DOCK_APP_IDS = stringPreferencesKey("dock_app_ids")
        // Stored page layouts in format: "id1,id2,id3|id4,id5,id6"
        val PAGES_LAYOUT = stringPreferencesKey("pages_layout")
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
            reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false
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
}
