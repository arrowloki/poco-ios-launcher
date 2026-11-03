package com.poco.launcher

import com.poco.launcher.core.model.AppItem
import com.poco.launcher.core.model.FolderItem
import com.poco.launcher.core.model.GridItem
import com.poco.launcher.core.model.LauncherSettings
import org.json.JSONObject
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

        val folderItem = FolderItem(
            id = "folder_1",
            title = "Utilities",
            appIds = listOf(app.id)
        )
        val folder = GridItem.Folder(
            folder = folderItem,
            apps = listOf(app)
        )

        assertEquals("folder_1", folder.id)
        assertEquals("Utilities", folder.title)
        assertEquals(1, folder.apps.size)
        assertEquals(app.id, folder.apps.first().id)
    }

    @Test
    fun testFolderSerializationFormat() {
        val folder1 = FolderItem(
            id = "f1",
            title = "Social",
            appIds = listOf("com.whatsapp/com.whatsapp.Main", "com.twitter/com.twitter.Main")
        )
        val folder2 = FolderItem(
            id = "f2",
            title = "Tools",
            appIds = listOf("com.calc/com.calc.Main")
        )
        val list = listOf(folder1, folder2)

        // Test serialization
        val serialized = list.joinToString(";;;") { folder ->
            "${folder.id}:::${folder.title}:::${folder.appIds.joinToString(",")}"
        }

        // Test deserialization
        val deserialized = serialized.split(";;;").mapNotNull { entry ->
            val parts = entry.split(":::")
            if (parts.size >= 3) {
                FolderItem(
                    id = parts[0],
                    title = parts[1],
                    appIds = parts[2].split(",").filter { it.isNotBlank() }
                )
            } else null
        }

        assertEquals(2, deserialized.size)
        assertEquals("f1", deserialized[0].id)
        assertEquals("Social", deserialized[0].title)
        assertEquals(2, deserialized[0].appIds.size)
        assertEquals("com.whatsapp/com.whatsapp.Main", deserialized[0].appIds[0])
        assertEquals("f2", deserialized[1].id)
        assertEquals("Tools", deserialized[1].title)
        assertEquals(1, deserialized[1].appIds.size)
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
        assertEquals("spotlight", settings.swipeDownAction)
    }

    @Test
    fun testJsonExportAndImportIntegrity() {
        val json = JSONObject()
        val settingsObj = JSONObject()
        settingsObj.put("gridColumns", 5)
        settingsObj.put("gridRows", 6)
        settingsObj.put("iconSizeDp", 64)
        settingsObj.put("showAppLabels", false)
        settingsObj.put("dockAppsCount", 5)
        settingsObj.put("themeMode", "dark")
        settingsObj.put("controlCenterEnabled", true)
        settingsObj.put("blurEnabled", false)
        settingsObj.put("reduceMotion", true)
        settingsObj.put("swipeDownAction", "control_center")

        json.put("settings", settingsObj)
        json.put("dockAppIds", "app1,app2,app3")
        json.put("folders", "f1:::Social:::app1,app2")

        val raw = json.toString()
        val parsed = JSONObject(raw)
        val parsedSettings = parsed.getJSONObject("settings")

        assertEquals(5, parsedSettings.getInt("gridColumns"))
        assertEquals(6, parsedSettings.getInt("gridRows"))
        assertEquals(64, parsedSettings.getInt("iconSizeDp"))
        assertFalse(parsedSettings.getBoolean("showAppLabels"))
        assertEquals(5, parsedSettings.getInt("dockAppsCount"))
        assertEquals("dark", parsedSettings.getString("themeMode"))
        assertTrue(parsedSettings.getBoolean("controlCenterEnabled"))
        assertFalse(parsedSettings.getBoolean("blurEnabled"))
        assertTrue(parsedSettings.getBoolean("reduceMotion"))
        assertEquals("control_center", parsedSettings.getString("swipeDownAction"))
        assertEquals("app1,app2,app3", parsed.getString("dockAppIds"))
        assertEquals("f1:::Social:::app1,app2", parsed.getString("folders"))
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
