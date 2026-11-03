package com.poco.launcher.core.model

import android.content.ComponentName
import android.os.Process
import android.os.UserHandle
import androidx.compose.runtime.Immutable

@Immutable
data class AppItem(
    val packageName: String,
    val className: String,
    val label: String,
    val userHandle: UserHandle? = null,
    val isSystemApp: Boolean = false,
    val installTime: Long = 0L
) {
    val id: String get() = "$packageName/$className"
    val componentName: ComponentName get() = ComponentName(packageName, className)
}

@Immutable
data class FolderItem(
    val id: String,
    val title: String,
    val appIds: List<String>
)

sealed interface GridItem {
    val id: String
    val title: String

    data class App(val app: AppItem) : GridItem {
        override val id: String get() = app.id
        override val title: String get() = app.label
    }

    data class Folder(
        val folder: FolderItem,
        val apps: List<AppItem>
    ) : GridItem {
        override val id: String get() = folder.id
        override val title: String get() = folder.title
    }
}

@Immutable
data class LauncherSettings(
    val gridColumns: Int = 4,
    val gridRows: Int = 6,
    val iconSizeDp: Int = 60,
    val showAppLabels: Boolean = true,
    val dockAppsCount: Int = 4,
    val themeMode: String = "system", // "system", "light", "dark"
    val controlCenterEnabled: Boolean = true,
    val blurEnabled: Boolean = true,
    val reduceMotion: Boolean = false
)
