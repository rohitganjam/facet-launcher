package com.facetlauncher.app.domain

import android.graphics.Bitmap
import android.graphics.RectF
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.di.DefaultDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Stores the framed part of a screenshot of the user's current wallpaper as the glass copy, without touching the system wallpaper (it is
 * already what the user wants). The current system id is recorded, so changing the wallpaper later shows the copy as stale.
 */
class UseScreenshotAsWallpaperCopyUseCase @Inject constructor(
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    private val settingsRepository: SettingsRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    /** `true` when the copy was stored. */
    suspend operator fun invoke(screenshot: Bitmap, visible: RectF): Boolean {
        val (width, height) = wallpaperCopyRepository.portraitScreenSize()
        val cropped = withContext(defaultDispatcher) { screenshot.croppedTo(visible, width, height) }
        val stored = wallpaperCopyRepository.save(cropped)
        if (stored) settingsRepository.setWallpaperCopySystemId(wallpaperCopyRepository.currentSystemWallpaperId())
        return stored
    }
}
