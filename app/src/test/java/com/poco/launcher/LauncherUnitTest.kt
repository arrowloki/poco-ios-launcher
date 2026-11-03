package com.poco.launcher

import com.poco.launcher.core.model.AppItem
import com.poco.launcher.core.model.GridItem
import com.poco.launcher.core.model.LauncherSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherUnitTest {

    @Test
    fun testAppItemProperties() {
        val app = AppItem(
            packageName = "com.android.calculator2",
            className = "com.android.calculator2.Calculator",
            label = "Calculator",
            isSystemApp = true
        )

        assertEquals("com.android.calculator2/com.android.calculator2.Calculator", app.id)
        assertEquals("Calculator", app.label)
        assertEquals("com.android.calculator2", app.packageName)
        assertEquals("com.android.calculator2.Calculator", app.className)
        assertTrue(app.isSystemApp)
    }

    @Test
    fun testGridItemHierarchy() {
        val app = AppItem(
            packageName = "com.android.settings",
            className = "com.android.settings.Settings",
            label = "Settings"
        )
        val gridApp: GridItem = GridItem.App(app)

        assertEquals(app.id, gridApp.id)
        assertEquals("Settings", gridApp.title)

        val folder = GridItem.Folder(
            id = "folder_1",
            title = "Utilities",
            apps = listOf(app)
        )

        assertEquals("folder_1", folder.id)
        assertEquals("Utilities", folder.title)
        assertEquals(1, folder.apps.size)
    }

    @Test
    fun testDefaultLauncherSettings() {
        val settings = LauncherSettings()

        assertEquals(4, settings.gridColumns)
        assertEquals(6, settings.gridRows)
        assertEquals(60, settings.iconSizeDp)
        assertTrue(settings.showAppLabels)
        assertEquals(4, settings.dockAppsCount)
        assertEquals("system", settings.themeMode)
        assertTrue(settings.controlCenterEnabled)
        assertTrue(settings.blurEnabled)
        assertFalse(settings.reduceMotion)
    }

    @Test
    fun testAppPaginationChunking() {
        val settings = LauncherSettings(gridColumns = 4, gridRows = 6)
        val itemsPerPage = settings.gridColumns * settings.gridRows // 24

        val mockApps = (1..50).map { i ->
            AppItem(
                packageName = "com.app.$i",
                className = "com.app.$i.MainActivity",
                label = "App $i"
            )
        }

        val pages = mockApps.chunked(itemsPerPage)
        assertEquals(3, pages.size)
        assertEquals(24, pages[0].size)
        assertEquals(24, pages[1].size)
        assertEquals(2, pages[2].size)
    }

    @Test
    fun testSearchFilterLogic() {
        val apps = listOf(
            AppItem("com.google.android.youtube", "com.google.android.youtube.MainActivity", "YouTube"),
            AppItem("com.whatsapp", "com.whatsapp.Main", "WhatsApp"),
            AppItem("com.spotify.music", "com.spotify.music.MainActivity", "Spotify"),
            AppItem("com.miui.gallery", "com.miui.gallery.activity.HomePageActivity", "Gallery")
        )

        val query = "you"
        val filtered = apps.filter {
            it.label.lowercase().contains(query) || it.packageName.lowercase().contains(query)
        }

        assertEquals(1, filtered.size)
        assertEquals("YouTube", filtered[0].label)
    }
}
