package com.facetlauncher.app.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.graphics.ImageDecoder
import android.net.Uri
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.ImageRegion
import com.facetlauncher.app.data.model.ScreenCopy
import com.facetlauncher.app.data.model.ScreenSize
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val LEGACY_COPY_FILE = "wallpaper_copy.png"
private const val COPY_PREFIX = "wallpaper_copy_"
private const val COPY_SUFFIX = ".png"
private const val SCREENS_FILE = "wallpaper_screens.txt"
private const val DISMISSED_FILE = "wallpaper_dismissed.txt"
private const val IDS_FILE = "wallpaper_copy_ids.txt"

/** [width] x [height] shrunk, keeping its shape, so the longer side is [maxSide]; `null` when it already fits. */
internal fun shrunkSize(width: Int, height: Int, maxSide: Int): Pair<Int, Int>? {
    val longest = max(width, height)
    if (longest <= maxSide) return null
    val ratio = maxSide.toFloat() / longest
    return (width * ratio).toInt().coerceAtLeast(1) to (height * ratio).toInt().coerceAtLeast(1)
}

/**
 * The wallpaper copy that Home's glass panels sample (Android does not let a launcher read the real wallpaper): a PNG in
 * app-private storage per screen size, since a foldable's screens differ. It stays on the phone and is not in backups.
 *
 * `open` so tests can substitute a fixed value.
 */
@Singleton
open class WallpaperCopyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wallpaperManager: WallpaperManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private fun fileFor(size: ScreenSize) = File(context.filesDir, "$COPY_PREFIX${size.key}$COPY_SUFFIX")
    private val copies = MutableStateFlow<Map<ScreenSize, Bitmap>>(emptyMap())
    private val seen = MutableStateFlow<Set<ScreenSize>>(emptySet())
    private val dismissed = MutableStateFlow<Set<String>>(emptySet())

    /** The system wallpaper's id when each screen's copy was stored (see [isCurrent] for a copy with none). */
    private val copyIds = MutableStateFlow<Map<ScreenSize, Int>>(emptyMap())

    /** Bumped by [recheck] so the copies are compared with the system wallpaper's id again. */
    private val checks = MutableStateFlow(0)
    private val mutex = Mutex()
    private var hasLoaded = false

    /** The size of the screen now showing, and again each time it changes (a foldable folding or unfolding). */
    open val currentScreen: Flow<ScreenSize> get() = callbackFlow {
        val manager = context.getSystemService(DisplayManager::class.java)
        trySend(screenNow())
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) = Unit
            override fun onDisplayRemoved(displayId: Int) = Unit
            override fun onDisplayChanged(displayId: Int) { trySend(screenNow()) }
        }
        manager.registerDisplayListener(listener, Handler(Looper.getMainLooper()))
        awaitClose { manager.unregisterDisplayListener(listener) }
    }.distinctUntilChanged()

    /**
     * The copy for the screen now showing: its own when that matches the wallpaper now, else the closest in shape that does (the backdrop
     * crops it to cover this screen), else its own or the closest out-of-date one; `null` when there is none at all. Every screen shown is
     * remembered, for [screens].
     */
    open val copy: Flow<Bitmap?> get() = choice().map { it.bitmap }

    /** Whether [copy] matches the system wallpaper now; `true` when there is no copy. Each screen is judged on its own, so a capture on one never clears another's notice. */
    open val copyIsCurrent: Flow<Boolean> get() = choice().map { it.current }

    private fun choice(): Flow<Choice> = combine(
        combine(copies.onStart { loadOnce() }, copyIds) { stored, ids -> stored to ids },
        currentScreen.onEach { noteSeen(it) },
        checks,
    ) { (stored, ids), screen, _ -> choose(stored, ids, screen, currentSystemWallpaperId()) }

    /** Every screen Facet has run on or holds a copy for (the one showing included), and whether each has a copy that matches the wallpaper now. */
    open val screens: Flow<List<ScreenCopy>> get() = combine(
        combine(copies.onStart { loadOnce() }, copyIds) { stored, ids -> stored to ids },
        seen,
        currentScreen,
        checks,
    ) { (stored, ids), known, _, _ ->
        val systemId = currentSystemWallpaperId()
        (known + stored.keys + screenNow()).sortedByDescending { it.area }.map {
            val current = it in stored && isCurrent(ids, it, systemId)
            ScreenCopy(it, current, outdated = it in stored && !current)
        }
    }

    /** Compares the copies with the system wallpaper's id again; call when the launcher comes back, since the wallpaper can change meanwhile. */
    open fun recheck() { checks.value++ }

    /** The prompts the user said no to (a screen's [promptKey], or [FOLDABLE_PROMPT]); they are not asked again. */
    open val dismissedPrompts: Flow<Set<String>> get() = dismissed.onStart { loadOnce() }

    /** Remembers that the user said no to the prompt with [key]. */
    open suspend fun dismissPrompt(key: String) {
        loadOnce()
        mutex.withLock {
            if (key in dismissed.value) return@withLock
            dismissed.value = dismissed.value + key
            withContext(ioDispatcher) { runCatching { File(context.filesDir, DISMISSED_FILE).writeText(dismissed.value.joinToString("\n")) } }
        }
    }

    /** `true` when the phone has a hinge sensor, so it is probably a foldable with a screen Facet has not seen. The feature flag is not used: an old emulator image sets it on a phone. */
    open fun maybeFoldable(): Boolean = context.getSystemService(SensorManager::class.java).getSensorList(Sensor.TYPE_HINGE_ANGLE).isNotEmpty()

    /** The largest screen known, by area: a wallpaper is set shaped for it, and the smaller screens show a centred part of it. */
    open suspend fun targetScreen(): ScreenSize = screens.first().first().size

    /** Every screen known, the largest first. */
    open suspend fun knownScreens(): List<ScreenSize> = screens.first().map { it.size }

    private fun screenNow(): ScreenSize = screenSize().let { (width, height) -> ScreenSize(width, height) }

    private class Choice(val bitmap: Bitmap?, val current: Boolean)

    /** A copy with no recorded id predates the tracking: it counts as current only while no copy has one, otherwise it may be from an older wallpaper. */
    private fun isCurrent(ids: Map<ScreenSize, Int>, size: ScreenSize, systemId: Int): Boolean =
        ids[size]?.let { it == NO_WALLPAPER_ID || it == systemId } ?: ids.isEmpty()

    private fun choose(stored: Map<ScreenSize, Bitmap>, ids: Map<ScreenSize, Int>, screen: ScreenSize, systemId: Int): Choice {
        val closest = compareBy<Map.Entry<ScreenSize, Bitmap>>({ kotlin.math.abs(it.key.aspect - screen.aspect) }, { -it.key.area })
        val fresh = stored.filter { isCurrent(ids, it.key, systemId) }
        val match = fresh[screen] ?: fresh.entries.minWithOrNull(closest)?.value
        return when {
            stored.isEmpty() -> Choice(null, true)
            match != null -> Choice(match, true)
            else -> Choice(stored[screen] ?: stored.entries.minWithOrNull(closest)?.value, false)
        }
    }

    /**
     * The screen's full size in pixels in its natural orientation (width, height), system bars included: the size of the copy. It does not
     * change when the phone is rotated, so a phone is one screen and a foldable's inner and outer displays are two.
     */
    open fun screenSize(): Pair<Int, Int> {
        val bounds = context.getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds
        val rotation = context.getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)?.rotation ?: Surface.ROTATION_0
        val turned = rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270
        return if (turned) bounds.height() to bounds.width() else bounds.width() to bounds.height()
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

    /** The lock screen wallpaper's id, or [NO_WALLPAPER_ID] when the lock screen shows the Home wallpaper. */
    open fun currentLockWallpaperId(): Int = runCatching { wallpaperManager.getWallpaperId(WallpaperManager.FLAG_LOCK) }.getOrDefault(NO_WALLPAPER_ID)

    /** The width and height of the encoded image in [bytes], read from its header; `null` when it is not an image. */
    open suspend fun imageSize(bytes: ByteArray): Pair<Int, Int>? = withContext(ioDispatcher) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        (options.outWidth to options.outHeight).takeIf { it.first > 0 && it.second > 0 }
    }

    /** Only [region] of the image in [bytes], at 1/[sampleSize] of its size, so a large photo is never decoded whole; `null` when it cannot be read. */
    open suspend fun decodeRegion(bytes: ByteArray, region: ImageRegion, sampleSize: Int): Bitmap? = withContext(ioDispatcher) {
        runCatching {
            @Suppress("DEPRECATION") // The non-deprecated overload needs API 31; minSdk is 30.
            val decoder = BitmapRegionDecoder.newInstance(bytes, 0, bytes.size, false) ?: return@runCatching null
            try {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                decoder.decodeRegion(Rect(region.left, region.top, region.right, region.bottom), options)
            } finally {
                decoder.recycle()
            }
        }.getOrNull()
    }

    /**
     * Sets [bitmap] as the system wallpaper, for the lock screen too when [includeLock], and returns the id it now has
     * (or [NO_WALLPAPER_ID] when the system refused).
     */
    open suspend fun setSystemWallpaper(bitmap: Bitmap, includeLock: Boolean): Int = withContext(ioDispatcher) {
        val which = if (includeLock) WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK else WallpaperManager.FLAG_SYSTEM
        val set = runCatching { wallpaperManager.setBitmap(bitmap, null, true, which) }.isSuccess
        if (set) currentSystemWallpaperId() else NO_WALLPAPER_ID
    }

    /** Stores [bitmap] as the copy for the screen now showing. */
    open suspend fun save(bitmap: Bitmap): Boolean = save(bitmap, screenNow())

    /** Stores [bitmap] as the copy for [size]. Written to a temp file first, so a failure never leaves a half-written copy. */
    open suspend fun save(bitmap: Bitmap, size: ScreenSize): Boolean = withContext(ioDispatcher) {
        val written = runCatching {
            val file = fileFor(size)
            val temp = File(context.filesDir, "${file.name}.tmp")
            temp.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
            check(temp.renameTo(file) || (file.delete() && temp.renameTo(file)))
        }.isSuccess
        if (written) mutex.withLock {
            hasLoaded = true
            copies.value = copies.value + (size to bitmap)
            copyIds.value = copyIds.value + (size to currentSystemWallpaperId())
            withContext(ioDispatcher) { runCatching { File(context.filesDir, IDS_FILE).writeText(copyIds.value.entries.joinToString("\n") { "${it.key.key} ${it.value}" }) } }
        }
        if (written) noteSeen(size)
        written
    }

    /** Deletes every copy; every panel goes back to its tint. The screens already seen stay remembered. */
    open suspend fun remove() {
        withContext(ioDispatcher) {
            context.filesDir.listFiles { file -> file.name.startsWith(COPY_PREFIX) || file.name == LEGACY_COPY_FILE }?.forEach { it.delete() }
            File(context.filesDir, IDS_FILE).delete()
        }
        mutex.withLock {
            hasLoaded = true
            copies.value = emptyMap()
            copyIds.value = emptyMap()
        }
    }

    /** Remembers [size] as a screen Facet runs on; a no-op when it is known already. */
    private suspend fun noteSeen(size: ScreenSize) {
        loadOnce()
        if (size in seen.value) return
        mutex.withLock {
            if (size in seen.value) return@withLock
            seen.value = seen.value + size
            withContext(ioDispatcher) { runCatching { File(context.filesDir, SCREENS_FILE).writeText(seen.value.joinToString("\n") { it.key }) } }
        }
    }

    private suspend fun loadOnce() = mutex.withLock {
        if (hasLoaded) return@withLock
        val storage = withContext(ioDispatcher) { readStorage() }
        copies.value = storage.copies
        copyIds.value = storage.ids + copyIds.value
        seen.value = seen.value + storage.seen
        dismissed.value = dismissed.value + storage.dismissed
        hasLoaded = true
    }

    /** Reads every stored copy and remembered screen. A copy from before per-screen storage is renamed to its own size. */
    private fun readStorage(): Storage {
        val stored = mutableMapOf<ScreenSize, Bitmap>()
        val legacy = File(context.filesDir, LEGACY_COPY_FILE)
        if (legacy.exists()) {
            BitmapFactory.decodeFile(legacy.path)?.let { bitmap ->
                val size = ScreenSize(bitmap.width, bitmap.height)
                if (legacy.renameTo(fileFor(size))) stored[size] = bitmap
            }
        }
        context.filesDir.listFiles { file -> file.name.startsWith(COPY_PREFIX) && file.name.endsWith(COPY_SUFFIX) }?.forEach { file ->
            val size = ScreenSize.parse(file.name.removePrefix(COPY_PREFIX).removeSuffix(COPY_SUFFIX)) ?: return@forEach
            if (size !in stored) BitmapFactory.decodeFile(file.path)?.let { stored[size] = it }
        }
        val known = File(context.filesDir, SCREENS_FILE).takeIf { it.exists() }?.readLines()?.mapNotNull { ScreenSize.parse(it.trim()) }.orEmpty()
        val said = File(context.filesDir, DISMISSED_FILE).takeIf { it.exists() }?.readLines()?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
        val ids = File(context.filesDir, IDS_FILE).takeIf { it.exists() }?.readLines().orEmpty().mapNotNull { line ->
            val (key, id) = line.trim().split(" ").takeIf { it.size == 2 } ?: return@mapNotNull null
            val size = ScreenSize.parse(key) ?: return@mapNotNull null
            id.toIntOrNull()?.let { size to it }
        }.toMap()
        return Storage(stored, known.toSet(), said.toSet(), ids)
    }

    private class Storage(val copies: Map<ScreenSize, Bitmap>, val seen: Set<ScreenSize>, val dismissed: Set<String>, val ids: Map<ScreenSize, Int>)

    companion object {
        /** The prompt that suggests unfolding to add the other screen of a foldable. */
        const val FOLDABLE_PROMPT = "foldable"

        /** What the system returns, and what is stored, when there is no wallpaper id to compare. */
        const val NO_WALLPAPER_ID = -1
        private const val PNG_QUALITY = 100
    }
}
