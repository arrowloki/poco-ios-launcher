package com.poco.launcher.core.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

class LauncherWidgetHost(
    context: Context,
    hostId: Int = HOST_ID
) : AppWidgetHost(context, hostId) {

    companion object {
        const val HOST_ID = 2048
    }

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidgetInfo: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return super.onCreateView(context, appWidgetId, appWidgetInfo)
    }
}
