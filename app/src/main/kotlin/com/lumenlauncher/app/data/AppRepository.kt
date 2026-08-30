package com.lumenlauncher.app.data

import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.lumenlauncher.app.data.model.AppInfo
import java.text.Collator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Wraps [LauncherApps] to expose the launchable apps visible to this launcher. */
@Singleton
class AppRepository @Inject constructor(private val launcherApps: LauncherApps) {

    /** All launchable activities for the current user, sorted alphabetically (locale-aware). */
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.Default) {
        val collator = Collator.getInstance()
        launcherApps.getActivityList(null, Process.myUserHandle())
            .map { info ->
                AppInfo(
                    packageName = info.applicationInfo.packageName,
                    activityName = info.componentName.className,
                    label = info.label.toString(),
                    icon = runCatching { info.getIcon(0).toBitmap().asImageBitmap() }.getOrNull(),
                )
            }
            .sortedWith(Comparator { a, b -> collator.compare(a.label, b.label) })
    }

    /**
     * Live-updating installed-app list — re-emits whenever a package is added, removed, or
     * changed while the launcher is running, via [LauncherApps.registerCallback]. [getInstalledApps]
     * only fetches once; without this, any install/uninstall happening while Lumen is in the
     * foreground (including via the app long-press context menu's Uninstall action) wouldn't be
     * reflected anywhere — Home/Drawer/Dock would keep showing the stale entry until the process
     * restarts. [FavoriteAppRepository]/[DockAppRepository] both hydrate against this live flow
     * too, so an uninstall collapses Favorites/Dock/Drawer alike; one-shot callers (picker seed
     * steps) can still use [getInstalledApps] directly where a live subscription isn't needed.
     */
    fun observeInstalledApps(): Flow<List<AppInfo>> = callbackFlow {
        val refresh: () -> Unit = { launch { trySend(getInstalledApps()) } }

        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String?, user: UserHandle?) = refresh()
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) = refresh()
            override fun onPackageChanged(packageName: String?, user: UserHandle?) = refresh()
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = refresh()
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = refresh()
        }
        launcherApps.registerCallback(callback)
        refresh()
        awaitClose { launcherApps.unregisterCallback(callback) }
    }

    /**
     * Emits a package name each time it's genuinely, permanently uninstalled — unlike
     * [observeInstalledApps], which also re-emits (the *whole* list) for reasons an app might
     * only be momentarily missing (e.g. `onPackagesUnavailable` mid-update). Backs
     * [com.lumenlauncher.app.domain.CleanUpUninstalledAppsUseCase], which deletes the
     * corresponding Favorites/Dock rows outright rather than just filtering them from view.
     */
    fun observeUninstalledPackages(): Flow<String> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String?, user: UserHandle?) = Unit
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                packageName?.let { trySend(it) }
            }
            override fun onPackageChanged(packageName: String?, user: UserHandle?) = Unit
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = Unit
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = Unit
        }
        launcherApps.registerCallback(callback)
        awaitClose { launcherApps.unregisterCallback(callback) }
    }
}
