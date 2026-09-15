package com.facetlauncher.app.data

import android.content.pm.LauncherApps
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.AppShortcut
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wraps [LauncherApps]'s app-shortcuts API (F12's "quick actions") — the same static/dynamic
 * shortcuts a stock Android launcher surfaces on long-press (e.g. Gmail's "Compose"). Reading
 * shortcuts requires this app to be the active default launcher
 * ([LauncherApps.hasShortcutHostPermission]); rather than throwing when it isn't (e.g. during
 * development, before the user has set Facet as default), this returns an empty list so the
 * long-press menu simply omits the quick-actions section.
 */
@Singleton
class AppShortcutRepository @Inject constructor(
    private val launcherApps: LauncherApps,
    private val appRepository: AppRepository,
) {

    companion object {
        private const val MAX_SHORTCUTS = 5
    }

    /** `emptyList()` (not a crash) when [profile] is [AppProfile.WORK] and no Work Profile handle can be resolved — same race-with-unenrollment reasoning as [AppRepository.resolveUserHandle]. */
    suspend fun getShortcuts(packageName: String, profile: AppProfile): List<AppShortcut> = withContext(Dispatchers.Default) {
        if (!launcherApps.hasShortcutHostPermission()) return@withContext emptyList()
        val handle = appRepository.resolveUserHandle(profile) ?: return@withContext emptyList()
        val query = LauncherApps.ShortcutQuery()
            .setPackage(packageName)
            .setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
            )
        runCatching { launcherApps.getShortcuts(query, handle) }
            .getOrNull()
            .orEmpty()
            .filter { it.isEnabled }
            .take(MAX_SHORTCUTS)
            .map { shortcut ->
                AppShortcut(
                    id = shortcut.id,
                    packageName = shortcut.`package`,
                    label = (shortcut.shortLabel ?: shortcut.longLabel ?: "").toString(),
                    icon = runCatching { launcherApps.getShortcutIconDrawable(shortcut, 0) }
                        .getOrNull()
                        ?.toBitmap()
                        ?.asImageBitmap(),
                    profile = profile,
                )
            }
    }

    /** Resolves [AppShortcut.profile] back to a real `UserHandle` — a no-op (not a crash) if that profile has since vanished (Work Profile unenrolled between fetch and tap). */
    fun launchShortcut(shortcut: AppShortcut) {
        val handle = appRepository.resolveUserHandle(shortcut.profile) ?: return
        runCatching {
            launcherApps.startShortcut(shortcut.packageName, shortcut.id, null, null, handle)
        }
    }
}
