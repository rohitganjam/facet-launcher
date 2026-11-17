package com.facetlauncher.app.domain

import android.content.Intent
import android.graphics.RectF
import com.facetlauncher.app.data.ScreenCaptureRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import javax.inject.Inject

/**
 * Takes one frame of the clean capture screen, after the user agreed in Android's dialog, and stores it as the glass copy. The frame is the
 * whole screen, with the system bars hidden or, if they would not hide, painted over, so nothing is cropped or framed. The wallpaper itself is left alone.
 */
class CaptureWallpaperUseCase @Inject constructor(
    private val screenCapture: ScreenCaptureRepository,
    private val useScreenshotAsCopy: UseScreenshotAsWallpaperCopyUseCase,
    private val wallpaperCopyRepository: WallpaperCopyRepository,
) {
    /** `true` when the copy was stored; `false` when no frame was taken, it is not in the screen's natural orientation (the copy is that shape), or it could not be stored. */
    suspend operator fun invoke(resultCode: Int, consent: Intent, barsHidden: () -> Boolean = { false }): Boolean {
        val frame = screenCapture.capture(resultCode, consent, barsHidden) ?: return false
        val (screenWidth, screenHeight) = wallpaperCopyRepository.screenSize()
        if ((frame.width > frame.height) != (screenWidth > screenHeight)) return false
        return useScreenshotAsCopy(frame, RectF(0f, 0f, frame.width.toFloat(), frame.height.toFloat()))
    }
}
