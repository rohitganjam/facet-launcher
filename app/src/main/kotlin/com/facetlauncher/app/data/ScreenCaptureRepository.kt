package com.facetlauncher.app.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

/**
 * One frame of the screen, taken with MediaProjection after the user agrees in Android's own dialog. Used only to copy the user's wallpaper
 * from a clean screen: nothing is recorded, nothing leaves the phone, and nothing runs unless the user asked for it.
 *
 * `open` so tests can substitute a fixed value.
 */
@Singleton
open class ScreenCaptureRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var pending: CompletableDeferred<Bitmap?>? = null

    /** Whether the capture screen has hidden both system bars right now; read by the service while it waits for them to go. */
    @Volatile internal var barsHidden: () -> Boolean = { false }

    /** The intent for Android's consent dialog; on Android 14 and up it asks for the whole screen, not a choice of one app. */
    open fun consentIntent(): Intent {
        val manager = context.getSystemService(MediaProjectionManager::class.java)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            manager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay())
        } else {
            manager.createScreenCaptureIntent()
        }
    }

    /**
     * One frame of the screen once the user has agreed ([resultCode] and [consent] are the dialog's result); `null` when it could not be taken.
     * The frame waits for [barsHidden] to be true, and the bars are painted over only if they never went.
     */
    open suspend fun capture(resultCode: Int, consent: Intent, barsHidden: () -> Boolean = { false }): Bitmap? {
        val result = CompletableDeferred<Bitmap?>()
        pending = result
        this.barsHidden = barsHidden
        try {
            runCatching { context.startForegroundService(ScreenCaptureService.startIntent(context, resultCode, consent)) }.onFailure { return null }
            return withTimeoutOrNull(CAPTURE_TIMEOUT_MS) { result.await() }
        } finally {
            pending = null
            this.barsHidden = { false }
            context.stopService(Intent(context, ScreenCaptureService::class.java))
        }
    }

    /** Called by the service with the frame it took, or `null` when it failed. */
    internal fun deliver(frame: Bitmap?) {
        pending?.complete(frame)
    }

    private companion object {
        const val CAPTURE_TIMEOUT_MS = 10_000L
    }
}

/**
 * A frame's pixels as a bitmap. A frame's rows are usually padded, so [rowStride] is more than [width] times [pixelStride]; the padding is
 * cut off.
 */
internal fun frameToBitmap(pixels: ByteBuffer, width: Int, height: Int, rowStride: Int, pixelStride: Int): Bitmap {
    val paddedWidth = rowStride / pixelStride
    val padded = Bitmap.createBitmap(paddedWidth, height, Bitmap.Config.ARGB_8888)
    padded.copyPixelsFromBuffer(pixels)
    return if (paddedWidth == width) padded else Bitmap.createBitmap(padded, 0, 0, width, height)
}

/**
 * Paints over the top [top] and bottom [bottom] rows, where the status bar, capture chip and navigation bar sit, with the rows next to them
 * mirrored, so the glass copy holds the wallpaper only. Bars covering half the frame or more are left as they are.
 */
internal fun Bitmap.withBarsFilled(top: Int, bottom: Int): Bitmap {
    val bars = top.coerceAtLeast(0) + bottom.coerceAtLeast(0)
    if (bars == 0 || bars >= height / 2) return this
    val result = if (isMutable) this else copy(Bitmap.Config.ARGB_8888, true)
    val row = IntArray(width)
    for (y in 0 until top.coerceAtLeast(0)) {
        getPixels(row, 0, width, 0, (2 * top - 1 - y).coerceAtMost(height - 1), width, 1)
        result.setPixels(row, 0, width, 0, y, width, 1)
    }
    val bottomStart = height - bottom.coerceAtLeast(0)
    for (y in bottomStart until height) {
        getPixels(row, 0, width, 0, (2 * bottomStart - 1 - y).coerceAtLeast(0), width, 1)
        result.setPixels(row, 0, width, 0, y, width, 1)
    }
    return result
}
