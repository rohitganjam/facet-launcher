package com.facetlauncher.app.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.content.IntentCompat
import com.facetlauncher.app.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Takes one frame of the screen for [ScreenCaptureRepository], or two when the repository can turn the screen, and stops. Android 14 and up needs this foreground service running before the
 * projection is created. The frame is taken after a short wait, so Android's dialog has gone and the screen shows only the wallpaper.
 */
@AndroidEntryPoint
class ScreenCaptureService : Service() {

    @Inject lateinit var repository: ScreenCaptureRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var display: VirtualDisplay? = null
    @Volatile private var latest: Bitmap? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val consent: Intent? = intent?.let { IntentCompat.getParcelableExtra(it, EXTRA_CONSENT, Intent::class.java) }
        startForeground(NOTIFICATION_ID, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        if (consent == null) {
            repository.deliver(null)
            stopSelf()
        } else {
            scope.launch { repository.deliver(runCatching { takeFrames(resultCode, consent) }.getOrNull()) }
        }
        return START_NOT_STICKY
    }

    private suspend fun takeFrames(resultCode: Int, consent: Intent): CapturedFrames? {
        val projection = getSystemService(MediaProjectionManager::class.java).getMediaProjection(resultCode, consent) ?: return null
        this.projection = projection
        // Android 14 requires a callback before the display is created.
        projection.registerCallback(object : MediaProjection.Callback() {}, Handler(Looper.getMainLooper()))
        val bounds = getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds
        val reader = newReader(bounds.width(), bounds.height())
        display = projection.createVirtualDisplay(
            "facet-wallpaper",
            bounds.width(),
            bounds.height(),
            resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            null,
        )
        val current = settledFrame() ?: return null
        val rotateTo = repository.rotateTo
        val turned = rotateTo?.let { runCatching { turnedFrame(it, landscape = current.width <= current.height) }.getOrNull() }
        return CapturedFrames(current, turned)
    }

    /** Turns the screen with [rotateTo], points the same display at its new size, and takes a frame; `null` unless it came out in the orientation asked for. */
    private suspend fun turnedFrame(rotateTo: suspend (Boolean) -> Boolean, landscape: Boolean): Bitmap? {
        if (!rotateTo(landscape)) return null
        val bounds = getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds
        if ((bounds.width() > bounds.height()) != landscape) return null
        latest = null
        val old = reader
        val next = newReader(bounds.width(), bounds.height())
        display?.resize(bounds.width(), bounds.height(), resources.displayMetrics.densityDpi)
        display?.surface = next.surface
        old?.close()
        return settledFrame()?.takeIf { (it.width > it.height) == landscape }
    }

    private fun newReader(width: Int, height: Int): ImageReader {
        val created = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        reader = created
        // The screen only produces a frame when it changes, so keep the newest and use it once the dialog has gone.
        created.setOnImageAvailableListener({ source ->
            source.acquireLatestImage()?.use { image ->
                val plane = image.planes[0]
                latest = frameToBitmap(plane.buffer, image.width, image.height, plane.rowStride, plane.pixelStride)
            }
        }, Handler(Looper.getMainLooper()))
        return created
    }

    /** The newest frame once the dialog or the turn has settled and the system bars are gone; their rows are painted over if they never went. */
    private suspend fun settledFrame(): Bitmap? {
        delay(SETTLE_MS)
        // Android's dialog brings the bars back; wait for the screen to hide them again, then for a frame without them.
        var barsWaited = 0L
        while (!repository.barsHidden() && barsWaited < BARS_TIMEOUT_MS) {
            delay(POLL_MS)
            barsWaited += POLL_MS
        }
        if (repository.barsHidden()) delay(BARS_SETTLE_MS)
        var waited = 0L
        while (latest == null && waited < FRAME_TIMEOUT_MS) {
            delay(POLL_MS)
            waited += POLL_MS
        }
        val metrics = getSystemService(WindowManager::class.java).maximumWindowMetrics
        val bars = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
        return if (repository.barsHidden()) latest else latest?.withBarsFilled(bars.top, bars.bottom)
    }

    override fun onDestroy() {
        scope.cancel()
        display?.release()
        reader?.close()
        projection?.stop()
        super.onDestroy()
    }

    private fun notification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.wallpaper_capture_channel), NotificationManager.IMPORTANCE_LOW),
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(getString(R.string.wallpaper_capture_notification))
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "wallpaper_capture"
        private const val NOTIFICATION_ID = 4101
        private const val EXTRA_RESULT_CODE = "result_code"
        private const val EXTRA_CONSENT = "consent"
        private const val SETTLE_MS = 700L
        private const val BARS_TIMEOUT_MS = 1_500L
        private const val BARS_SETTLE_MS = 300L
        private const val FRAME_TIMEOUT_MS = 3_000L
        private const val POLL_MS = 50L

        internal fun startIntent(context: Context, resultCode: Int, consent: Intent) =
            Intent(context, ScreenCaptureService::class.java)
                .putExtra(EXTRA_RESULT_CODE, resultCode)
                .putExtra(EXTRA_CONSENT, consent)
    }
}
