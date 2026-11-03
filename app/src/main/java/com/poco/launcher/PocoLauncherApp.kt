package com.poco.launcher

import android.app.Application
import com.poco.launcher.core.datastore.LauncherDataStore
import com.poco.launcher.core.icon.IconCacheManager
import com.poco.launcher.core.repository.AppRepository
import com.poco.launcher.core.system.SystemControllers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PocoLauncherApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var iconCacheManager: IconCacheManager
        private set

    lateinit var launcherDataStore: LauncherDataStore
        private set

    lateinit var appRepository: AppRepository
        private set

    lateinit var systemControllers: SystemControllers
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        iconCacheManager = IconCacheManager(this)
        launcherDataStore = LauncherDataStore(this)
        appRepository = AppRepository(this, applicationScope)
        systemControllers = SystemControllers(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            iconCacheManager.clear()
        }
    }

    companion object {
        lateinit var instance: PocoLauncherApp
            private set
    }
}
