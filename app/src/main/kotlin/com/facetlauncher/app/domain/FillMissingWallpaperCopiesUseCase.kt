package com.facetlauncher.app.domain

import android.graphics.RectF
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.di.DefaultDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Builds the copies a wallpaper set from a picked photo is missing, from the original kept with it: a screen first seen later (a foldable
 * unfolded for the first time), or an orientation not stored. Only while that wallpaper is still the one set, so a wallpaper changed elsewhere
 * is never drawn from an old photo (which is then deleted). Does nothing when no copy is missing or there is no original (a captured wallpaper has to be captured again).
 */
class FillMissingWallpaperCopiesUseCase @Inject constructor(
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    /** `true` when at least one copy was built. */
    suspend operator fun invoke(): Boolean {
        val keptId = wallpaperCopyRepository.originalSystemId()
        if (keptId != null && keptId != wallpaperCopyRepository.currentSystemWallpaperId()) {
            // The wallpaper was changed elsewhere: this photo is not it any more, and is never drawn again.
            wallpaperCopyRepository.deleteOriginal()
            return false
        }
        val missing = wallpaperCopyRepository.screens.first().filter { !it.hasCopy }
        if (missing.isEmpty()) return false
        val original = wallpaperCopyRepository.original() ?: return false
        val photo = wallpaperCopyRepository.decodeFile(original.file, MAX_ORIGINAL_SIDE) ?: return false
        val framing = original.framing
        val visible = RectF(framing.left * photo.width, framing.top * photo.height, framing.right * photo.width, framing.bottom * photo.height)
        val region = wallpaperRegion(visible, photo.width, photo.height)
        val (width, height) = wallpaperSize(region, wallpaperCopyRepository.knownScreens())
        val wallpaper = withContext(defaultDispatcher) { photo.croppedTo(region, width, height) }
        return missing.map { view -> wallpaperCopyRepository.save(withContext(defaultDispatcher) { wallpaper.copyFor(view.size) }, view.size) }.any { it }
    }

    private companion object {
        /** A large photo is not decoded whole; the framing is kept as fractions, so it lands on the same part at any size. */
        const val MAX_ORIGINAL_SIDE = 3072
    }
}
