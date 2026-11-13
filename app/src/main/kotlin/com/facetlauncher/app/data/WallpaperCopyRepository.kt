package com.facetlauncher.app.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.view.WindowManager
import com.facetlauncher.app.data.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val COPY_FILE = "wallpaper_copy.png"

/** [width] x [height] shrunk, keeping its shape, so the longer side is [maxSide]; `null` when it already fits. */
internal fun shrunkSize(width: Int, height: Int, maxSide: Int): Pair<Int, Int>? {
    val longest = max(width, height)
    if (longest <= maxSide) return null
    val ratio = maxSide.toFloat() / longest
    return (width * ratio).toInt().coerceAtLeast(1) to (height * ratio).toInt().coerceAtLeast(1)
}

/**
 * The wallpaper copy that Home's glass panels sample (Android does not let a launcher read the real wallpaper): a PNG in
 * app-private storage, the size of the screen. It stays on the phone and is not in backups.
 *
 * `open` so tests can substitute a fixed value.
 */
@Singleton
open class WallpaperCopyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wallpaperManager: WallpaperManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val file get() = File(context.filesDir, COPY_FILE)
    private val loaded = MutableStateFlow<Bitmap?>(null)
    private val mutex = Mutex()
    private var hasLoaded = false

    /** The stored copy, `null` when there is none; the file is read once, on the first collection. */
    open val copy: Flow<Bitmap?> get() = loaded.onStart { loadOnce() }

    /** The screen's full size in pixels as a portrait pair (width, height), system bars included: the size of the copy. */
    open fun portraitScreenSize(): Pair<Int, Int> {
        val bounds = context.getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds
        return min(bounds.width(), bounds.height()) to max(bounds.width(), bounds.height())
    }

    /**
     * The photo at [uri], turned upright as its metadata says and shrunk so its long side is at most [maxSide];
     * `null` when it cannot be read. It is a software bitmap so it can be cropped and set as the wallpaper.
     */
    open suspend fun decodePicked(uri: Uri, maxSide: Int): Bitmap? = withContext(ioDispatcher) {
        runCatching {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                shrunkSize(info.size.width, info.size.height, maxSide)?.let { (width, height) -> decoder.setTargetSize(width, height) }
            }
        }.getOrNull()
    }

    /** Like [decodePicked], for an image already in memory: upright, shrunk to [maxSide] on its long side, software; `null` when it cannot be read. */
    open suspend fun decodeBytes(bytes: ByteArray, maxSide: Int): Bitmap? = withContext(ioDispatcher) {
        runCatching {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(ByteBuffer.wrap(bytes))) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                shrunkSize(info.size.width, info.size.height, maxSide)?.let { (width, height) -> decoder.setTargetSize(width, height) }
            }
        }.getOrNull()
    }

    /** The id of the system wallpaper now; it changes whenever anyone sets a new one, so it tells a stale copy. */
    open fun currentSystemWallpaperId(): Int = runCatching { wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM) }.getOrDefault(NO_WALLPAPER_ID)

    /**
     * Sets [bitmap] as the system wallpaper, for the lock screen too when [includeLock], and returns the id it now has
     * (or [NO_WALLPAPER_ID] when the system refused).
     */
    open suspend fun setSystemWallpaper(bitmap: Bitmap, includeLock: Boolean): Int = withContext(ioDispatcher) {
        val which = if (includeLock) WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK else WallpaperManager.FLAG_SYSTEM
        val set = runCatching { wallpaperManager.setBitmap(bitmap, null, true, which) }.isSuccess
        if (set) currentSystemWallpaperId() else NO_WALLPAPER_ID
    }

    /** Stores [bitmap] as the copy. Written to a temp file first, so a failure never leaves a half-written copy. */
    open suspend fun save(bitmap: Bitmap): Boolean = withContext(ioDispatcher) {
        val written = runCatching {
            val temp = File(context.filesDir, "$COPY_FILE.tmp")
            temp.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
            check(temp.renameTo(file) || (file.delete() && temp.renameTo(file)))
        }.isSuccess
        if (written) mutex.withLock {
            hasLoaded = true
            loaded.value = bitmap
        }
        written
    }

    /** Deletes the copy; every panel goes back to its tint. */
    open suspend fun remove() {
        withContext(ioDispatcher) { file.delete() }
        mutex.withLock {
            hasLoaded = true
            loaded.value = null
        }
    }

    private suspend fun loadOnce() = mutex.withLock {
        if (hasLoaded) return@withLock
        loaded.value = withContext(ioDispatcher) { file.takeIf { it.exists() }?.let { BitmapFactory.decodeFile(it.path) } }
        hasLoaded = true
    }

    companion object {
        /** What the system returns, and what is stored, when there is no wallpaper id to compare. */
        const val NO_WALLPAPER_ID = -1
        private const val PNG_QUALITY = 100
    }
}
