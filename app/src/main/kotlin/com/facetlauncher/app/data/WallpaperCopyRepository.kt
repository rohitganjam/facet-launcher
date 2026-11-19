package com.facetlauncher.app.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.graphics.RectF
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
import com.facetlauncher.app.domain.wallpaperOffsetX
import com.facetlauncher.app.data.model.promptKey
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val LEGACY_COPY_FILE = "wallpaper_copy.png"
private const val COPY_PREFIX = "wallpaper_copy_"
private const val COPY_SUFFIX = ".jpg"

/** Copies made before they were JPEGs; still read, replaced the next time that view is saved. */
private const val OLD_COPY_SUFFIX = ".png"
private const val SCREENS_FILE = "wallpaper_screens.txt"
private const val DISMISSED_FILE = "wallpaper_dismissed.txt"
private const val IDS_FILE = "wallpaper_copy_ids.txt"
private const val SHAPE_FILE = "wallpaper_shape.txt"
private const val ORIGINAL_FILE = "wallpaper_original.bin"
private const val ORIGINAL_FRAMING_FILE = "wallpaper_original_framing.txt"

/** [width] x [height] shrunk, keeping its shape, so the longer side is [maxSide]; `null` when it already fits. */
internal fun shrunkSize(width: Int, height: Int, maxSide: Int): Pair<Int, Int>? {
    val longest = max(width, height)
    if (longest <= maxSide) return null
    val ratio = maxSide.toFloat() / longest
    return (width * ratio).toInt().coerceAtLeast(1) to (height * ratio).toInt().coerceAtLeast(1)
}

/**
 * The wallpaper copies that Home's glass panels sample (Android does not let a launcher read the real wallpaper): a PNG in
 * app-private storage per view, a screen's pixel size in one orientation. A foldable's screens differ, and the system crops the wallpaper
 * differently for each orientation, so a phone has a portrait copy and a landscape one. The copy for a view is drawn as it is, never
 * turned or re-cropped; a view without one has none. It stays on the phone and is not in backups.
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
    private fun oldFileFor(size: ScreenSize) = File(context.filesDir, "$COPY_PREFIX${size.key}$OLD_COPY_SUFFIX")
    private val copies = MutableStateFlow<Map<ScreenSize, Bitmap>>(emptyMap())
    private val finishing = MutableStateFlow(0)

    /** `true` while the other views' copies of a wallpaper just set are still being stored, so a view lacking one is not suggested yet. */
    open val isFinishingCopies: Flow<Boolean> get() = finishing.map { it > 0 }.distinctUntilChanged()

    /** Marks that copies are being stored; every call is paired with a [stopFinishingCopies], by whoever ends the work. */
    open fun startFinishingCopies() = finishing.update { it + 1 }

    open fun stopFinishingCopies() = finishing.update { it - 1 }
    private val seen = MutableStateFlow<Set<ScreenSize>>(emptySet())
    private val dismissed = MutableStateFlow<Set<String>>(emptySet())

    /** The system wallpaper's id when each screen's copy was stored (see [isCurrent] for a copy with none). */
    private val copyIds = MutableStateFlow<Map<ScreenSize, Int>>(emptyMap())

    /** Bumped by [recheck] so the copies are compared with the system wallpaper's id again. */
    private val checks = MutableStateFlow(0)
    private val mutex = Mutex()
    private var hasLoaded = false

    /** The view now showing: the window's size in its current orientation, and again each time it changes (a rotation, or a foldable folding or unfolding). */
    open val currentScreen: Flow<ScreenSize> get() = callbackFlow {
        val manager = context.getSystemService(DisplayManager::class.java)
        trySend(windowNow())
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) = Unit
            override fun onDisplayRemoved(displayId: Int) = Unit
            override fun onDisplayChanged(displayId: Int) { trySend(windowNow()) }
        }
        manager.registerDisplayListener(listener, Handler(Looper.getMainLooper()))
        awaitClose { manager.unregisterDisplayListener(listener) }
    }.distinctUntilChanged()

    /**
     * The copy for the view now showing, matching it exactly (its screen and orientation), whether or not it matches the wallpaper now
     * (see [copyIsCurrent]); `null` when the view has none. A copy is never borrowed from another view. Every screen shown is remembered,
     * for [screens].
     */
    open val copy: Flow<Bitmap?> get() = choice().map { it.bitmap }

    /** Whether [copy] matches the system wallpaper now; `true` when there is no copy. Each view is judged on its own, so a capture on one never clears another's notice. */
    open val copyIsCurrent: Flow<Boolean> get() = choice().map { it.current }

    /** `true` when any view has a copy, so one that is missing is a gap to fill rather than glass never having been set up. */
    open val hasCopyAnywhere: Flow<Boolean> get() = copies.onStart { loadOnce() }.map { it.isNotEmpty() }.distinctUntilChanged()

    private fun choice(): Flow<Choice> = combine(
        combine(copies.onStart { loadOnce() }, copyIds) { stored, ids -> stored to ids },
        currentScreen.onEach { noteSeen(screenNow()) },
        checks,
    ) { (stored, ids), view, _ -> choose(stored, ids, view, currentSystemWallpaperId()) }

    /**
     * Every view of every screen Facet has run on or holds a copy for (the one showing included), the larger screens first and each
     * screen's own orientation before its turned one, and whether each has a copy that matches the wallpaper now.
     */
    open val screens: Flow<List<ScreenCopy>> get() = combine(
        combine(copies.onStart { loadOnce() }, copyIds) { stored, ids -> stored to ids },
        seen,
        currentScreen,
        checks,
    ) { (stored, ids), known, _, _ ->
        val systemId = currentSystemWallpaperId()
        naturalSizes(known, stored.keys).flatMap { screen ->
            listOf(screen, screen.turned).distinct().map { view ->
                val current = view in stored && isCurrent(ids, view, systemId)
                ScreenCopy(view, current, outdated = view in stored && !current, screen = screen)
            }
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

    /** The largest screen known, by area, in its natural orientation: a wallpaper is set shaped for it, and the smaller screens show a centred part of it. */
    open suspend fun targetScreen(): ScreenSize = knownScreens().first()

    /** Every screen known in its natural orientation, the largest first. */
    open suspend fun knownScreens(): List<ScreenSize> {
        loadOnce()
        return naturalSizes(seen.value, copies.value.keys)
    }

    /**
     * The natural sizes of the screens behind [known] (the screens seen) and [stored] (the copies held): a copy of a seen screen turned is
     * not another screen, and one of an unseen size stands for its own screen. The showing screen is always included, the largest first.
     */
    private fun naturalSizes(known: Set<ScreenSize>, stored: Set<ScreenSize>): List<ScreenSize> {
        val base = known + screenNow()
        return (base + stored.filter { it !in base && it.turned !in base }).sortedByDescending { it.area }
    }

    private fun screenNow(): ScreenSize = screenSize().let { (width, height) -> ScreenSize(width, height) }

    private fun windowNow(): ScreenSize = windowSize().let { (width, height) -> ScreenSize(width, height) }

    private class Choice(val bitmap: Bitmap?, val current: Boolean)

    /** A copy with no recorded id predates the tracking: it counts as current only while no copy has one, otherwise it may be from an older wallpaper. */
    private fun isCurrent(ids: Map<ScreenSize, Int>, size: ScreenSize, systemId: Int): Boolean =
        ids[size]?.let { it == NO_WALLPAPER_ID || it == systemId } ?: ids.isEmpty()

    /** The copy for exactly [view]; `current` is whether it matches the wallpaper now, `true` when the view has none (that is a gap, not a stale copy). */
    private fun choose(stored: Map<ScreenSize, Bitmap>, ids: Map<ScreenSize, Int>, view: ScreenSize, systemId: Int): Choice =
        stored[view]?.let { Choice(it, isCurrent(ids, view, systemId)) } ?: Choice(null, true)

    /**
     * The screen's full size in pixels in its natural orientation (width, height), system bars included: the size of the copy. It does not
     * change when the phone is rotated, so a phone is one screen and a foldable's inner and outer displays are two.
     */
    open fun screenSize(): Pair<Int, Int> {
        val (width, height) = windowSize()
        val rotation = context.getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)?.rotation ?: Surface.ROTATION_0
        val turned = rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270
        return if (turned) height to width else width to height
    }

    /** The screen's full size in pixels in the orientation it is in now (width, height): the size of a copy taken now. A phone turned sideways is its landscape view. */
    open fun windowSize(): Pair<Int, Int> {
        val bounds = context.getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds
        return bounds.width() to bounds.height()
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

    /** `true` when the wallpaper is a live one: it moves, so a copy, a still frame, can only approximate it. */
    open fun isLiveWallpaper(): Boolean = runCatching { wallpaperManager.wallpaperInfo != null }.getOrDefault(false)

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

    /** Remembers the size of the wallpaper Facet set (id [systemId]) and whether it was framed in landscape, so its offset can be worked out later. */
    open fun recordWallpaperShape(systemId: Int, width: Int, height: Int, framedLandscape: Boolean) {
        runCatching { File(context.filesDir, SHAPE_FILE).writeText("$systemId $width $height ${if (framedLandscape) 1 else 0}") }
    }

    /**
     * The horizontal offset to give the system for the wallpaper showing: worked out for the orientation it was framed in, when Facet set it, and the
     * middle (0.5) in the other orientation or for a wallpaper Facet did not set.
     */
    open fun wallpaperOffsetX(): Float {
        val parts = runCatching { File(context.filesDir, SHAPE_FILE).takeIf { it.exists() }?.readText()?.trim()?.split(' ')?.map { it.toInt() } }.getOrNull()
        val (screenWidth, screenHeight) = windowSize()
        val matches = parts != null && parts.size == SHAPE_FIELDS && parts[0] == currentSystemWallpaperId() && (parts[3] == 1) == (screenWidth > screenHeight)
        return if (matches) wallpaperOffsetX(parts!![1], parts[2], screenWidth, screenHeight) else DEFAULT_OFFSET
    }

    /** Stores [bitmap] as the copy for the view now showing. */
    open suspend fun save(bitmap: Bitmap): Boolean = save(bitmap, windowNow())

    /** Stores [bitmap] as the copy for [size]. Written to a temp file first, so a failure never leaves a half-written copy. */
    open suspend fun save(bitmap: Bitmap, size: ScreenSize): Boolean = withContext(ioDispatcher) {
        val written = runCatching {
            val file = fileFor(size)
            val temp = File(context.filesDir, "${file.name}.tmp")
            temp.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
            check(temp.renameTo(file) || (file.delete() && temp.renameTo(file)))
            oldFileFor(size).delete()
        }.isSuccess
        if (written) mutex.withLock {
            hasLoaded = true
            copies.value = copies.value + (size to bitmap)
            copyIds.value = copyIds.value + (size to currentSystemWallpaperId())
            withContext(ioDispatcher) { runCatching { File(context.filesDir, IDS_FILE).writeText(copyIds.value.entries.joinToString("\n") { "${it.key.key} ${it.value}" }) } }
        }
        if (written) noteSeen(screenNow().takeIf { size == it || size == it.turned } ?: size)
        written
    }

    /**
     * Keeps the image the wallpaper was made from, untouched, with the [framing] the user chose (as fractions of the photo as it is shown,
     * upright) and the system wallpaper's [systemId] it was set as, so copies can be built for a screen first seen later. Replaces any earlier one.
     */
    open suspend fun saveOriginal(bytes: ByteArray, framing: RectF, systemId: Int): Boolean =
        writeOriginal(framing, systemId) { it.writeBytes(bytes) }

    /** Like [saveOriginal], streaming the image at [uri] to storage, so a very large photo is never held in memory. */
    open suspend fun saveOriginal(uri: Uri, framing: RectF, systemId: Int): Boolean = writeOriginal(framing, systemId) { target ->
        context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { input.copyTo(it) } } ?: error("cannot open $uri")
    }

    private suspend fun writeOriginal(framing: RectF, systemId: Int, write: (File) -> Unit): Boolean = withContext(ioDispatcher) {
        runCatching {
            val file = File(context.filesDir, ORIGINAL_FILE)
            val temp = File(context.filesDir, "$ORIGINAL_FILE.tmp")
            write(temp)
            check(temp.renameTo(file) || (file.delete() && temp.renameTo(file)))
            File(context.filesDir, ORIGINAL_FRAMING_FILE).writeText("$systemId ${framing.left} ${framing.top} ${framing.right} ${framing.bottom}")
        }.onFailure { File(context.filesDir, "$ORIGINAL_FILE.tmp").delete() }.isSuccess
    }

    /** The image kept by [saveOriginal] (a file, not read into memory), or `null` when there is none. */
    open suspend fun original(): StoredOriginal? = withContext(ioDispatcher) {
        runCatching {
            val file = File(context.filesDir, ORIGINAL_FILE).takeIf { it.exists() } ?: return@runCatching null
            val parts = File(context.filesDir, ORIGINAL_FRAMING_FILE).readText().trim().split(" ")
            val values = parts.drop(1).map { it.toFloat() }
            StoredOriginal(file, RectF(values[0], values[1], values[2], values[3]), parts[0].toInt())
        }.getOrNull()
    }

    /** The system wallpaper id the kept original was set as, without reading the image; `null` when there is none. */
    open suspend fun originalSystemId(): Int? = withContext(ioDispatcher) {
        runCatching { File(context.filesDir, ORIGINAL_FRAMING_FILE).readText().trim().split(" ").first().toInt() }.getOrNull()
    }

    /** Deletes the kept original and its framing; the copies stay. */
    open suspend fun deleteOriginal() {
        withContext(ioDispatcher) {
            File(context.filesDir, ORIGINAL_FILE).delete()
            File(context.filesDir, ORIGINAL_FRAMING_FILE).delete()
        }
    }

    /** Like [decodeBytes], for an image kept in a file. */
    open suspend fun decodeFile(file: File, maxSide: Int): Bitmap? = withContext(ioDispatcher) {
        runCatching {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                shrunkSize(info.size.width, info.size.height, maxSide)?.let { (width, height) -> decoder.setTargetSize(width, height) }
            }
        }.getOrNull()
    }

    /** Deletes every copy and the kept original; every panel goes back to its tint. The screens already seen stay remembered. */
    open suspend fun remove() {
        withContext(ioDispatcher) {
            context.filesDir.listFiles { file -> file.name.startsWith(COPY_PREFIX) || file.name == LEGACY_COPY_FILE }?.forEach { it.delete() }
            File(context.filesDir, IDS_FILE).delete()
            File(context.filesDir, ORIGINAL_FILE).delete()
            File(context.filesDir, ORIGINAL_FRAMING_FILE).delete()
        }
        mutex.withLock {
            hasLoaded = true
            copies.value = emptyMap()
            copyIds.value = emptyMap()
        }
    }

    /**
     * Remembers [size] as a screen Facet runs on; a no-op when it is known already. A screen of the same shape at another size is the same
     * screen at another resolution (the display size was changed): the old size is forgotten, with its copies, so it is not asked about for ever.
     */
    private suspend fun noteSeen(size: ScreenSize) {
        loadOnce()
        if (size in seen.value && seen.value.none { it.isResolutionOf(size) }) return
        mutex.withLock {
            val old = seen.value.filter { it.isResolutionOf(size) }.toSet()
            if (size in seen.value && old.isEmpty()) return@withLock
            seen.value = seen.value - old + size
            withContext(ioDispatcher) { runCatching { File(context.filesDir, SCREENS_FILE).writeText(seen.value.joinToString("\n") { it.key }) } }
            if (old.isNotEmpty()) forget(old)
        }
    }

    /** Whether this is [other]'s screen at another resolution: not the same size, but the same shape. */
    private fun ScreenSize.isResolutionOf(other: ScreenSize) = this != other && kotlin.math.abs(aspect - other.aspect) <= other.aspect * SAME_SHAPE_TOLERANCE

    /** Drops the copies and ids of screens that no longer exist, in both orientations, and what the user said no to for them. Called with the lock held. */
    private suspend fun forget(screens: Set<ScreenSize>) {
        val views = screens.flatMap { listOf(it, it.turned) }.toSet()
        copies.value = copies.value - views
        copyIds.value = copyIds.value - views
        dismissed.value = dismissed.value.filterNot { key -> views.any { it.promptKey == key } }.toSet()
        withContext(ioDispatcher) {
            views.forEach {
                fileFor(it).delete()
                oldFileFor(it).delete()
            }
            runCatching { File(context.filesDir, IDS_FILE).writeText(copyIds.value.entries.joinToString("\n") { "${it.key.key} ${it.value}" }) }
            runCatching { File(context.filesDir, DISMISSED_FILE).writeText(dismissed.value.joinToString("\n")) }
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
                if (legacy.renameTo(oldFileFor(size))) stored[size] = bitmap
            }
        }
        // A JPEG copy first, so it wins over an old PNG of the same view.
        listOf(COPY_SUFFIX, OLD_COPY_SUFFIX).forEach { suffix ->
            context.filesDir.listFiles { file -> file.name.startsWith(COPY_PREFIX) && file.name.endsWith(suffix) }?.forEach { file ->
                val size = ScreenSize.parse(file.name.removePrefix(COPY_PREFIX).removeSuffix(suffix)) ?: return@forEach
                if (size !in stored) BitmapFactory.decodeFile(file.path)?.let { stored[size] = it }
            }
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

    /** The image a wallpaper was made from: its [file] as picked, the [framing] chosen as fractions of the upright photo, and the system wallpaper id it was set as. */
    class StoredOriginal(val file: File, val framing: RectF, val systemId: Int)

    private class Storage(val copies: Map<ScreenSize, Bitmap>, val seen: Set<ScreenSize>, val dismissed: Set<String>, val ids: Map<ScreenSize, Int>)

    companion object {
        /** The prompt that suggests unfolding to add the other screen of a foldable. */
        const val FOLDABLE_PROMPT = "foldable"

        /** What the system returns, and what is stored, when there is no wallpaper id to compare. */
        const val NO_WALLPAPER_ID = -1
        private const val SHAPE_FIELDS = 4
        private const val DEFAULT_OFFSET = 0.5f
        private const val JPEG_QUALITY = 95

        /** How much two screens' shapes may differ and still be one screen at two resolutions. */
        private const val SAME_SHAPE_TOLERANCE = 0.01f
    }
}
