package com.facetlauncher.app.domain

import android.graphics.RectF
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.dailyphotos.DailyWallpaperRepository
import com.facetlauncher.app.data.model.DailyWallpaperProblem
import java.time.Clock
import javax.inject.Inject

/**
 * Sets the photo [RunDailyWallpaperUseCase] prepared, through [SetWallpaperFromPhotoUseCase] so the glass copy is the same image, and records
 * it. Called at screen-off, so nobody sees Home change or an app restart for the new wallpaper's colours. `true` when a wallpaper was set.
 */
class ApplyPendingWallpaperUseCase @Inject constructor(
    private val dailyWallpaperRepository: DailyWallpaperRepository,
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    private val setWallpaperFromPhoto: SetWallpaperFromPhotoUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Boolean {
        val state = dailyWallpaperRepository.get()
        val photo = state.pending
        if (!state.enabled || state.paused) return false
        if (photo == null) {
            // An interrupted change whose photo is gone: take what is set now as the baseline, so it isn't mistaken for the user's change.
            if (state.applying) adoptCurrentIds()
            return false
        }
        if (state.changedElsewhere(wallpaperCopyRepository.currentSystemWallpaperId(), wallpaperCopyRepository.currentLockWallpaperId())) {
            dailyWallpaperRepository.deletePending()
            dailyWallpaperRepository.update { it.copy(paused = true, pending = null, problem = null) }
            return false
        }
        val bitmap = dailyWallpaperRepository.readPending()
        if (bitmap == null) {
            // The cache was cleared: prepare again on the next resume.
            dailyWallpaperRepository.update { it.copy(pending = null, checkedDay = null, applying = false) }
            return false
        }
        dailyWallpaperRepository.update { it.copy(applying = true) }
        val set = setWallpaperFromPhoto(bitmap, RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()), state.includeLock)
        if (!set) {
            dailyWallpaperRepository.deletePending()
            dailyWallpaperRepository.update { it.copy(enabled = false, pending = null, applying = false, problem = DailyWallpaperProblem.REFUSED) }
            return false
        }
        val systemId = wallpaperCopyRepository.currentSystemWallpaperId()
        val lockId = wallpaperCopyRepository.currentLockWallpaperId()
        dailyWallpaperRepository.update {
            it.copy(
                pending = null,
                applying = false,
                current = photo,
                currentSetAtMillis = clock.millis(),
                systemId = systemId,
                lockId = lockId,
                history = (listOf(photo.imageUrl) + it.history).distinct(),
                problem = null,
            )
        }
        dailyWallpaperRepository.deletePending()
        return true
    }

    private suspend fun adoptCurrentIds() {
        val systemId = wallpaperCopyRepository.currentSystemWallpaperId()
        val lockId = wallpaperCopyRepository.currentLockWallpaperId()
        dailyWallpaperRepository.update { it.copy(applying = false, systemId = systemId, lockId = lockId) }
    }
}
