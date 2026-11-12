package com.facetlauncher.app.domain

import android.graphics.Bitmap
import android.graphics.RectF
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.di.DefaultDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** The part of this photo in [visible], scaled to [width] x [height]: the copy that is stored and set as the wallpaper. */
internal fun Bitmap.croppedTo(visible: RectF, width: Int, height: Int): Bitmap {
    val left = visible.left.toInt().coerceIn(0, this.width - 1)
    val top = visible.top.toInt().coerceIn(0, this.height - 1)
    val cropWidth = visible.width().toInt().coerceIn(1, this.width - left)
    val cropHeight = visible.height().toInt().coerceIn(1, this.height - top)
    val cropped = Bitmap.createBitmap(this, left, top, cropWidth, cropHeight)
    return if (cropWidth == width && cropHeight == height) cropped else Bitmap.createScaledBitmap(cropped, width, height, true)
}

/**
 * Turns the part of a picked photo the user framed into the wallpaper: crops it to the screen's size, sets it as the system wallpaper
 * (the lock screen too when asked), stores it as the copy glass panels sample, and records the system wallpaper's id so a later change
 * elsewhere shows as a stale copy. Nothing is stored unless the system accepted the wallpaper, so the copy never differs from what is set.
 */
class SetWallpaperFromPhotoUseCase @Inject constructor(
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    private val settingsRepository: SettingsRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    /** `true` when the wallpaper was set and the copy stored. */
    suspend operator fun invoke(photo: Bitmap, visible: RectF, includeLock: Boolean): Boolean {
        val (width, height) = wallpaperCopyRepository.portraitScreenSize()
        val cropped = withContext(defaultDispatcher) { photo.croppedTo(visible, width, height) }
        val systemId = wallpaperCopyRepository.setSystemWallpaper(cropped, includeLock)
        val stored = systemId != WallpaperCopyRepository.NO_WALLPAPER_ID && wallpaperCopyRepository.save(cropped)
        if (stored) settingsRepository.setWallpaperCopySystemId(systemId)
        return stored
    }
}
