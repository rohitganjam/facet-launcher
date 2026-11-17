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
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
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
 * Takes one frame of the screen for [ScreenCaptureRepository] and stops. Android 14 and up needs this foreground service running before the
 * projection is created. The frame is taken after a short wait, so Android's dialog has gone and the screen shows only the wallpaper.
 */
@AndroidEntryPoint
class ScreenCaptureService : Service() {

    @Inject lateinit var repository: ScreenCaptureRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        @Suppress("DEPRECATION") // The typed overload needs API 33; minSdk is 30.
        val consent: Intent? = intent?.getParcelableExtra(EXTRA_CONSENT)
        startForeground(NOTIFICATION_ID, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        if (consent == null) {
            repository.deliver(null)
            stopSelf()
        } else {
            scope.launch { repository.deliver(runCatching { takeFrame(resultCode, consent) }.getOrNull()) }
        }
        return START_NOT_STICKY
    }

    private suspend fun takeFrame(resultCode: Int, consent: Intent): Bitmap? {
        val manager = getSystemService(MediaProjectionManager::class.java)
        val projection = manager.getMediaProjection(resultCode, consent) ?: return null
        this.projection = projection
        // Android 14 requires a callback before the display is created.
        projection.registerCallback(object : MediaProjection.Callback() {}, Handler(Looper.getMainLooper()))
        val metrics = getSystemService(WindowManager::class.java).maximumWindowMetrics
        val bounds = metrics.bounds
        val bars = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
        val reader = ImageReader.newInstance(bounds.width(), bounds.height(), PixelFormat.RGBA_8888, 2)
        this.reader = reader
        var latest: Bitmap? = null
        // The screen only produces a frame when it changes, so keep the newest and use it once the dialog has gone.
        reader.setOnImageAvailableListener({ source ->
            source.acquireLatestImage()?.use { image ->
                val plane = image.planes[0]
                latest = frameToBitmap(plane.buffer, image.width, image.height, plane.rowStride, plane.pixelStride)
            }
        }, Handler(Looper.getMainLooper()))
        projection.createVirtualDisplay(
            "facet-wallpaper",
            bounds.width(),
            bounds.height(),
            resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            null,
        )
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
        // If the bars would not hide, they and Android's capture chip are in the frame, so they are painted over.
        return if (repository.barsHidden()) latest else latest?.withBarsFilled(bars.top, bars.bottom)
    }

    override fun onDestroy() {
        scope.cancel()
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
