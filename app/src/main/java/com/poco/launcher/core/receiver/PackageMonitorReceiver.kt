package com.poco.launcher.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.poco.launcher.PocoLauncherApp

/**
 * Event-driven BroadcastReceiver that detects when applications are installed,
 * uninstalled, updated, or modified on the POCO device.
 * Eliminates periodic polling and ensures instant UI updates.
 */
class PackageMonitorReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val packageName = intent.data?.schemeSpecificPart

        val app = context.applicationContext as? PocoLauncherApp ?: return

        if (!packageName.isNullOrBlank()) {
            app.iconCacheManager.invalidate(packageName)
        }

        when (action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_CHANGED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                app.appRepository.refreshApps()
            }
        }
    }
}
