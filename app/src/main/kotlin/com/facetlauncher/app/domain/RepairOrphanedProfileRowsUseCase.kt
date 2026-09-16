package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.model.AppProfile
import javax.inject.Inject

/**
 * One-time-per-row backfill for Favorites/Dock/Folder rows that predate
 * `Migrations.MIGRATION_19_20` (the real `userId` identity column) — every row created before
 * that migration only ever had a `profile` display category stored, never the real `UserHandle`
 * it came from, so the migration itself could only deterministically backfill `PERSONAL` rows
 * (the primary user is always id `0`); anything else was left at the migration's `-1` sentinel.
 *
 * For each orphaned row, resolves its stored [AppProfile] against the *currently live* set of
 * profiles via [AppRepository.handlesFor]: exactly one live handle matching that category
 * backfills it; zero or more than one candidate leaves it orphaned — it simply won't hydrate
 * against the live app list until the user re-adds it (a graceful degradation, not a crash or
 * silent misattribution to the wrong profile). A no-op once every row has been resolved (or
 * genuinely can't be), so it's safe and cheap to run unconditionally on every launch — wired
 * alongside [CleanUpUninstalledAppsUseCase] in
 * [com.facetlauncher.app.ui.launcher.LauncherViewModel], but as a single suspend pass rather than
 * a live `Flow` collector, since there's nothing to keep observing once it's done.
 */
class RepairOrphanedProfileRowsUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val folderRepository: FolderRepository,
) {
    suspend operator fun invoke() {
        favoriteAppRepository.getOrphanedRows().forEach { entity ->
            resolve(entity.profile)?.let { userId -> favoriteAppRepository.backfillUserId(entity, userId) }
        }
        dockAppRepository.getOrphanedRows().forEach { entity ->
            resolve(entity.profile)?.let { userId -> dockAppRepository.backfillUserId(entity, userId) }
        }
        facetDockAppRepository.getOrphanedRows().forEach { entity ->
            resolve(entity.profile)?.let { userId -> facetDockAppRepository.backfillUserId(entity, userId) }
        }
        defaultFavoriteAppRepository.getOrphanedRows().forEach { entity ->
            resolve(entity.profile)?.let { userId -> defaultFavoriteAppRepository.backfillUserId(entity, userId) }
        }
        folderRepository.getOrphanedRows().forEach { entity ->
            resolve(entity.profile)?.let { userId -> folderRepository.backfillUserId(entity, userId) }
        }
    }

    /** `null` (leave orphaned) unless exactly one live handle matches [profile] — see this class's own doc. */
    private fun resolve(profile: AppProfile): Int? = appRepository.handlesFor(profile).singleOrNull()?.hashCode()
}
