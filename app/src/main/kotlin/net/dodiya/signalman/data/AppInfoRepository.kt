package net.dodiya.signalman.data

import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class AppInfoRepository(
    private val packageManager: PackageManager,
    private val myPackageName: String,
) {
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: Flow<List<AppInfo>> = _installedApps.asStateFlow()

    suspend fun loadInstalledApps() {
        withContext(Dispatchers.IO) {
            val mainIntent =
                Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
            val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)

            val apps =
                resolveInfos
                    .map {
                        AppInfo(
                            name = it.loadLabel(packageManager).toString(),
                            packageName = it.activityInfo.packageName,
                            icon = it.loadIcon(packageManager),
                        )
                    }.distinctBy { it.packageName }
                    .filter { it.packageName != myPackageName }
                    .sortedBy { it.name }

            _installedApps.update { apps }
        }
    }
}
