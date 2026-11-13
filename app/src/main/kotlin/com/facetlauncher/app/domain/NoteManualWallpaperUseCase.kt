package com.facetlauncher.app.domain

import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.dailyphotos.DailyWallpaperRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * The user just set a wallpaper in Facet's own fit step. A Bing or Spotlight photo keeps the daily change on and counts as today's; their own
 * image turns it off (the fit step says so before they set it). Either way the prepared photo is dropped.
 */
class NoteManualWallpaperUseCase @Inject constructor(
    private val dailyWallpaperRepository: DailyWallpaperRepository,
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(fromDailyPhoto: Boolean) {
        if (!dailyWallpaperRepository.get().enabled) return
        dailyWallpaperRepository.deletePending()
        val systemId = wallpaperCopyRepository.currentSystemWallpaperId()
        val lockId = wallpaperCopyRepository.currentLockWallpaperId()
        val today = LocalDate.now(clock)
        dailyWallpaperRepository.update {
            if (fromDailyPhoto) {
                it.copy(pending = null, applying = false, paused = false, checkedDay = today, current = null, currentSetAtMillis = null, systemId = systemId, lockId = lockId, problem = null)
            } else {
                it.copy(enabled = false, pending = null, applying = false, paused = false, problem = null)
            }
        }
    }
}
