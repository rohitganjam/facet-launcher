package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.LUMEN_LAUNCHER_PACKAGE_NAME
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Fetches every launchable app visible to this launcher, alphabetically sorted. */
class GetInstalledAppsUseCase @Inject constructor(private val appRepository: AppRepository) {
    /** One-shot fetch — for pickers (Favorites/Dock) that just need a seed list, not live updates. */
    suspend operator fun invoke(): List<AppInfo> = appRepository.getInstalledApps()

    /**
     * Live-updating list for the App Drawer — re-emits on install/uninstall/update while
     * collected (see [AppRepository.observeInstalledApps]), excluding this launcher's own entry
     * (launching yourself from within yourself is meaningless). The one-shot [invoke] above is
     * deliberately left untouched — Favorites/Dock pickers use it, and a user pinning their own
     * launcher there is a harmless, if unusual, choice to leave available.
     */
    fun observe(): Flow<List<AppInfo>> = appRepository.observeInstalledApps().map { apps ->
        apps.filterNot { it.packageName == LUMEN_LAUNCHER_PACKAGE_NAME }
    }
}
