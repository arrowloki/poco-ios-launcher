package com.poco.launcher.core.icon

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import com.poco.launcher.core.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-performance, memory-efficient Icon Cache for POCO devices.
 * Masks app icons into iOS-style continuous squircles and caches them in memory.
 */
class IconCacheManager(private val context: Context) {

    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
    private val packageManager = context.packageManager

    // Standard icon resolution: 192x192 px provides ultra-crisp visuals on FHD+/QHD+ POCO screens
    private val iconSize = 192

    // Max cache: 24MB to ensure smooth operation even on 3GB/4GB RAM POCO devices
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = (maxMemory / 8).coerceAtMost(24 * 1024)

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    // Pre-allocated paint and path for masking
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }
    private val xferPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        isFilterBitmap = true
    }

    /**
     * Get or load an app icon asynchronously. Returns cached bitmap if available.
     */
    suspend fun getAppIcon(app: AppItem): Bitmap = withContext(Dispatchers.Default) {
        val cached = memoryCache.get(app.id)
        if (cached != null) return@withContext cached

        val bitmap = loadAndProcessIcon(app)
        memoryCache.put(app.id, bitmap)
        bitmap
    }

    /**
     * Synchronous check for existing cached bitmap.
     */
    fun getCachedIcon(appId: String): Bitmap? = memoryCache.get(appId)

    /**
     * Invalidate cached icons for an updated or removed package.
     */
    fun invalidate(packageName: String) {
        val snapshot = memoryCache.snapshot()
        for (key in snapshot.keys) {
            if (key.startsWith(packageName)) {
                memoryCache.remove(key)
            }
        }
    }

    fun clear() {
        memoryCache.evictAll()
    }

    private fun loadAndProcessIcon(app: AppItem): Bitmap {
        val drawable = try {
            if (launcherApps != null) {
                val activities = launcherApps.getActivityList(app.packageName, app.userHandle ?: android.os.Process.myUserHandle())
                val match = activities.firstOrNull { it.componentName.className == app.className }
                    ?: activities.firstOrNull()
                match?.getBadgedIcon(context.resources.configuration.densityDpi)
            } else {
                packageManager.getActivityIcon(app.componentName)
            }
        } catch (e: Exception) {
            null
        } ?: try {
            packageManager.getApplicationIcon(app.packageName)
        } catch (e: Exception) {
            ColorDrawable(0xFF007AFF.toInt())
        }

        return renderSquircleBitmap(drawable)
    }

    private fun renderSquircleBitmap(drawable: Drawable): Bitmap {
        val rawBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(rawBitmap)

        drawable.setBounds(0, 0, iconSize, iconSize)
        drawable.draw(canvas)

        // Create the masked squircle bitmap
        val output = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val outputCanvas = Canvas(output)

        // Draw original content
        outputCanvas.drawBitmap(rawBitmap, 0f, 0f, maskPaint)

        // Create and apply squircle mask
        val maskBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(maskBitmap)
        val squirclePath = createAndroidSquirclePath(iconSize.toFloat(), iconSize.toFloat())
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
        }
        maskCanvas.drawPath(squirclePath, fillPaint)

        outputCanvas.drawBitmap(maskBitmap, 0f, 0f, xferPaint)

        rawBitmap.recycle()
        maskBitmap.recycle()

        return output
    }

    private fun createAndroidSquirclePath(w: Float, h: Float, ratio: Float = 0.225f): Path {
        val path = Path()
        val minSide = minOf(w, h)
        val r = minSide * ratio
        val c = r * 0.551915024494f

        path.moveTo(r, 0f)
        path.lineTo(w - r, 0f)
        path.cubicTo(w - r + c, 0f, w, r - c, w, r)
        path.lineTo(w, h - r)
        path.cubicTo(w, h - r + c, w - r + c, h, w - r, h)
        path.lineTo(r, h)
        path.cubicTo(r - c, h, 0f, h - r + c, 0f, h - r)
        path.lineTo(0f, r)
        path.cubicTo(0f, r - c, r - c, 0f, r, 0f)
        path.close()

        return path
    }
}
