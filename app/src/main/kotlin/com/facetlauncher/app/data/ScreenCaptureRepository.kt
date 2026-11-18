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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withTimeoutOrNull

/** What one capture session took: [current] in the orientation the screen was in, and [turned] in the other one when the session could turn the screen. */
class CapturedFrames(val current: Bitmap, val turned: Bitmap? = null)

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
    private var pending: CompletableDeferred<CapturedFrames?>? = null
    private val running = MutableStateFlow(false)

    /** `true` from the user's go-ahead until the frames are stored, so Home does not offer a capture that is already being made. */
    open val capturing: StateFlow<Boolean> get() = running

    /** Runs [block] with [capturing] true, however it ends. */
    open suspend fun <T> whileCapturing(block: suspend () -> T): T {
        running.value = true
        try {
            return block()
        } finally {
            running.value = false
        }
    }

    /** Turns the screen to landscape (`true`) or portrait for the second frame, and returns once it has turned (`false` when it did not); read by the service. */
    @Volatile internal var rotateTo: (suspend (landscape: Boolean) -> Boolean)? = null

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
     * The frame waits for [barsHidden] to be true, and the bars are painted over only if they never went. With [rotateTo] the same session then
     * turns the screen and takes a second frame in the other orientation, so one approval covers both; if that part fails only the first is returned.
     */
    open suspend fun capture(
        resultCode: Int,
        consent: Intent,
        barsHidden: () -> Boolean = { false },
        rotateTo: (suspend (landscape: Boolean) -> Boolean)? = null,
    ): CapturedFrames? {
        val result = CompletableDeferred<CapturedFrames?>()
        pending = result
        this.barsHidden = barsHidden
        this.rotateTo = rotateTo
        try {
            runCatching { context.startForegroundService(ScreenCaptureService.startIntent(context, resultCode, consent)) }.onFailure { return null }
            return withTimeoutOrNull(if (rotateTo == null) CAPTURE_TIMEOUT_MS else CAPTURE_BOTH_TIMEOUT_MS) { result.await() }
        } finally {
            pending = null
            this.barsHidden = { false }
            this.rotateTo = null
            context.stopService(Intent(context, ScreenCaptureService::class.java))
        }
    }

    /** Called by the service with the frames it took, or `null` when it failed. */
    internal fun deliver(frames: CapturedFrames?) {
        pending?.complete(frames)
    }

    private companion object {
        const val CAPTURE_TIMEOUT_MS = 10_000L
        const val CAPTURE_BOTH_TIMEOUT_MS = 25_000L
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
