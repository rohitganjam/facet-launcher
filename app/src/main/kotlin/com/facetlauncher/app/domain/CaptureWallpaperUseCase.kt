package com.facetlauncher.app.domain

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import com.facetlauncher.app.data.ScreenCaptureRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.model.ScreenSize
import javax.inject.Inject

/**
 * Takes one frame of the clean capture screen, after the user agreed in Android's dialog, and stores it as the glass copy; with [rotateTo] the same
 * session turns the screen and also stores the other orientation's. A frame is the whole screen, with the system bars hidden or, if they would not
 * hide, painted over, so nothing is cropped or framed. The wallpaper itself is left alone.
 */
class CaptureWallpaperUseCase @Inject constructor(
    private val screenCapture: ScreenCaptureRepository,
    private val useScreenshotAsCopy: UseScreenshotAsWallpaperCopyUseCase,
    private val wallpaperCopyRepository: WallpaperCopyRepository,
) {
    /**
     * `true` when the copy for the orientation the screen was in was stored; `false` when no frame was taken, it is not in that orientation (the copy
     * is that shape), or it could not be stored. The other orientation's copy is stored too when it was taken, and its failure does not fail the first.
     */
    suspend operator fun invoke(
        resultCode: Int,
        consent: Intent,
        barsHidden: () -> Boolean = { false },
        rotateTo: (suspend (landscape: Boolean) -> Boolean)? = null,
    ): Boolean = screenCapture.whileCapturing { captureAndStore(resultCode, consent, barsHidden, rotateTo) }

    private suspend fun captureAndStore(
        resultCode: Int,
        consent: Intent,
        barsHidden: () -> Boolean,
        rotateTo: (suspend (landscape: Boolean) -> Boolean)?,
    ): Boolean {
        val (windowWidth, windowHeight) = wallpaperCopyRepository.windowSize()
        val view = ScreenSize(windowWidth, windowHeight)
        val frames = screenCapture.capture(resultCode, consent, barsHidden, rotateTo) ?: return false
        return store(frames.current, view).also { if (it) frames.turned?.let { turned -> store(turned, view.turned) } }
    }

    /** Stores [frame] whole as the copy for [view] when it is in that view's orientation. */
    private suspend fun store(frame: Bitmap, view: ScreenSize): Boolean =
        (frame.width > frame.height) == view.isLandscape && useScreenshotAsCopy(frame, RectF(0f, 0f, frame.width.toFloat(), frame.height.toFloat()), view)
}
