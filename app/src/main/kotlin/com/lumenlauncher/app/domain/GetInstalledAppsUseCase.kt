package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.model.AppInfo
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** Fetches every launchable app visible to this launcher, alphabetically sorted. */
class GetInstalledAppsUseCase @Inject constructor(private val appRepository: AppRepository) {
    /** One-shot fetch — for pickers (Favorites/Dock) that just need a seed list, not live updates. */
    suspend operator fun invoke(): List<AppInfo> = appRepository.getInstalledApps()

    /** Live-updating list — re-emits on install/uninstall/update while collected. See [AppRepository.observeInstalledApps]. */
    fun observe(): Flow<List<AppInfo>> = appRepository.observeInstalledApps()
}
