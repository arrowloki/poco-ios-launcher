package com.poco.launcher.core.repository

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Process
import android.os.UserHandle
import com.poco.launcher.core.model.AppItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Single source of truth for installed applications on the device.
 * Optimized for POCO smartphones to prevent main-thread PackageManager stalls.
 */
class AppRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
    private val packageManager = context.packageManager

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        refreshApps()
    }

    fun refreshApps() {
        scope.launch(Dispatchers.Default) {
            _isLoading.value = true
            val apps = loadInstalledApps()
            _installedApps.value = apps
            _isLoading.value = false
        }
    }

    private fun loadInstalledApps(): List<AppItem> {
        val result = mutableListOf<AppItem>()
        val myPackageName = context.packageName

        try {
            if (launcherApps != null) {
                val userHandle = Process.myUserHandle()
                val activities: List<LauncherActivityInfo> = launcherApps.getActivityList(null, userHandle)

                for (info in activities) {
                    val pkg = info.applicationInfo.packageName
                    if (pkg == myPackageName) continue // Don't show ourselves on the grid

                    val label = info.label?.toString() ?: pkg
                    val isSystem = (info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val installTime = try {
                        packageManager.getPackageInfo(pkg, 0).firstInstallTime
                    } catch (e: Exception) {
                        0L
                    }

                    result.add(
                        AppItem(
                            packageName = pkg,
                            className = info.componentName.className,
                            label = label,
                            userHandle = userHandle,
                            isSystemApp = isSystem,
                            installTime = installTime
                        )
                    )
                }
            } else {
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = packageManager.queryIntentActivities(intent, 0)
                for (info in resolveInfos) {
                    val pkg = info.activityInfo.packageName
                    if (pkg == myPackageName) continue

                    val label = info.loadLabel(packageManager)?.toString() ?: pkg
                    val isSystem = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                    result.add(
                        AppItem(
                            packageName = pkg,
                            className = info.activityInfo.name,
                            label = label,
                            isSystemApp = isSystem
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sort alphabetically by default for fast binary search and clean display
        return result.sortedBy { it.label.lowercase() }
    }

    /**
     * Launch an application with smooth native Android transition options.
     */
    fun launchApp(app: AppItem): Boolean {
        return try {
            if (launcherApps != null) {
                launcherApps.startMainActivity(
                    app.componentName,
                    app.userHandle ?: Process.myUserHandle(),
                    null,
                    ActivityOptions.makeCustomAnimation(
                        context,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    ).toBundle()
                )
                true
            } else {
                val intent = packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                    component = app.componentName
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                } ?: Intent(Intent.ACTION_MAIN).apply {
                    component = app.componentName
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }

                val options = ActivityOptions.makeCustomAnimation(
                    context,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                context.startActivity(intent, options.toBundle())
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Search installed apps with sub-millisecond filtering.
     */
    fun searchApps(query: String): List<AppItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()

        return _installedApps.value.filter {
            it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
        }
    }
}
