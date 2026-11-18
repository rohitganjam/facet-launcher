package com.facetlauncher.app.domain

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
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

/** The image a wallpaper was made from, to be kept: a downloaded file in memory, or a picked one streamed from where it is. */
sealed interface KeptOriginal {
    class Bytes(val bytes: ByteArray) : KeptOriginal
    class Picked(val uri: Uri) : KeptOriginal
}

/** What setting a wallpaper did: whether it was [set] and its copies stored, and whether its original was [originalKept] (`true` when there was none to keep). */
data class SetWallpaperResult(val set: Boolean, val originalKept: Boolean = true)

/**
 * Turns the part of a picked photo the user framed into the wallpaper: sets [wallpaperRegion] of it (the framing widened evenly as far as the photo has room, up to a square) as the system wallpaper (the lock screen too when asked), stores what each view of every
 * known screen shows of it, in both orientations, as that view's copy, and records the system wallpaper's id so a later change elsewhere
 * shows as a stale copy. With the photo's [original] file it also keeps that, and the framing (as fractions of the original, [originalFraming]
 * when [photo] is only a part of it), to build copies for a screen first seen later. Nothing is stored unless the system accepted the wallpaper, so the copies never differ from what is set.
 */
class SetWallpaperFromPhotoUseCase @Inject constructor(
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    private val settingsRepository: SettingsRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    /** [SetWallpaperResult.set] is `true` when the wallpaper was set and the copies stored; [SetWallpaperResult.originalKept] is `false` when [original] could not be kept. */
    suspend operator fun invoke(photo: Bitmap, visible: RectF, includeLock: Boolean, original: KeptOriginal? = null, originalFraming: RectF? = null): SetWallpaperResult {
        val screens = wallpaperCopyRepository.knownScreens()
        val region = wallpaperRegion(visible, photo.width, photo.height)
        val (width, height) = wallpaperSize(region, screens)
        val wallpaper = withContext(defaultDispatcher) { photo.croppedTo(region, width, height) }
        val systemId = wallpaperCopyRepository.setSystemWallpaper(wallpaper, includeLock)
        if (systemId == WallpaperCopyRepository.NO_WALLPAPER_ID) return SetWallpaperResult(set = false)
        wallpaperCopyRepository.recordWallpaperShape(systemId, wallpaper.width, wallpaper.height, region.width() > region.height())
        val stored = screens.flatMap { listOf(it, it.turned).distinct() }.map { view ->
            wallpaperCopyRepository.save(withContext(defaultDispatcher) { wallpaper.copyFor(view) }, view)
        }.all { it }
        val kept = stored && record(systemId, originalFraming ?: photo.fractionsOf(visible), original)
        return SetWallpaperResult(set = stored, originalKept = kept || original == null)
    }

    private fun Bitmap.fractionsOf(part: RectF) = RectF(part.left / width, part.top / height, part.right / width, part.bottom / height)

    /** `true` when the original, if there is one, was kept. */
    private suspend fun record(systemId: Int, framing: RectF, original: KeptOriginal?): Boolean {
        settingsRepository.setWallpaperCopySystemId(systemId)
        return when (original) {
            null -> true
            is KeptOriginal.Bytes -> wallpaperCopyRepository.saveOriginal(original.bytes, framing, systemId)
            is KeptOriginal.Picked -> wallpaperCopyRepository.saveOriginal(original.uri, framing, systemId)
        }
    }
}
