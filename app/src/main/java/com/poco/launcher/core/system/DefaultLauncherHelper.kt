package com.poco.launcher.core.system

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object DefaultLauncherHelper {

    fun isDefaultLauncher(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.resolveActivity(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            }
            val defaultPackage = resolveInfo?.activityInfo?.packageName
            defaultPackage == context.packageName
        } catch (e: Exception) {
            false
        }
    }

    fun createSetDefaultLauncherIntent(context: Context): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                }
            }
        }
        return null
    }

    fun openDefaultLauncherSettings(context: Context) {
        Toast.makeText(
            context,
            "Select 'Default apps' → 'Home app' → 'POCO Launcher'",
            Toast.LENGTH_LONG
        ).show()

        // 1. Try MIUI specific intent
        try {
            val miuiIntent = Intent("miui.intent.action.PREFERRED_APP").apply {
                putExtra("preferred_app_type", "home")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (miuiIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(miuiIntent)
                return
            }
        } catch (e: Exception) {
            // continue
        }

        // 2. Try Android standard Home settings
        try {
            val homeSettings = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (homeSettings.resolveActivity(context.packageManager) != null) {
                context.startActivity(homeSettings)
                return
            }
        } catch (e: Exception) {
            // continue
        }

        // 3. Try Manage Default Apps settings
        try {
            val defaultApps = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (defaultApps.resolveActivity(context.packageManager) != null) {
                context.startActivity(defaultApps)
                return
            }
        } catch (e: Exception) {
            // continue
        }

        // 4. Try general application settings
        try {
            val appSettings = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(appSettings)
        } catch (e: Exception) {
            val generic = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(generic)
        }
    }
}
