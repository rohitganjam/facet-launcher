package com.facetlauncher.app.domain

import android.app.WallpaperManager
import android.graphics.Bitmap
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.DeviceConditionsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.dailyphotos.DailyPhotoApi
import com.facetlauncher.app.data.dailyphotos.DailyPhotoCache
import com.facetlauncher.app.data.dailyphotos.DailyPhotoFeed
import com.facetlauncher.app.data.dailyphotos.DailyPhotoRepository
import com.facetlauncher.app.data.dailyphotos.DailyWallpaperRepository
import com.facetlauncher.app.data.model.DailyPhoto
import com.facetlauncher.app.data.model.DailyPhotoSource
import com.facetlauncher.app.data.model.ImageRegion
import com.facetlauncher.app.data.model.NetworkConditions
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.mockito.Mockito.mock
import org.robolectric.RuntimeEnvironment

/** Shared stand-ins for the daily wallpaper tests: in-memory pending photo, a wallpaper store with ids the test sets, feeds and downloads it controls. */
internal val dailyTestClock: Clock = Clock.fixed(Instant.parse("2026-11-13T08:00:00Z"), ZoneOffset.UTC)

internal fun dailyPhoto(name: String, date: String? = null) =
    DailyPhoto(title = name, credit = "© $name", thumbUrl = "https://img.peapix.com/${name}_640.jpg", imageUrl = "https://img.peapix.com/$name.jpg", date = date)

internal class FakeDailyWallpaperRepository(folder: File) : DailyWallpaperRepository(
    dataStore = PreferenceDataStoreFactory.create(produceFile = { File(folder, "daily-${System.nanoTime()}.preferences_pb") }),
    context = RuntimeEnvironment.getApplication(),
    ioDispatcher = Dispatchers.Unconfined,
) {
    var pendingBitmap: Bitmap? = null
    var saves = true

    override suspend fun savePending(bitmap: Bitmap): Boolean {
        if (saves) pendingBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        return saves
    }
    override suspend fun readPending(): Bitmap? = pendingBitmap
    override suspend fun deletePending() { pendingBitmap = null }
}

internal class FakeDailyCopies : WallpaperCopyRepository(RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java)) {
    var systemId = 10
    var lockId = NO_WALLPAPER_ID
    var setSucceeds = true
    var setCount = 0
    var setIncludeLock: Boolean? = null
    var saved: Bitmap? = null

    /** The size each downloaded image reports, by its first byte. */
    val sizes = mutableMapOf<Byte, Pair<Int, Int>>()
    val decodedRegions = mutableListOf<Pair<ImageRegion, Int>>()

    override fun portraitScreenSize() = 100 to 200
    override fun currentSystemWallpaperId() = systemId
    override fun currentLockWallpaperId() = lockId
    override suspend fun imageSize(bytes: ByteArray): Pair<Int, Int>? = sizes[bytes.first()]
    override suspend fun decodeRegion(bytes: ByteArray, region: ImageRegion, sampleSize: Int): Bitmap? {
        decodedRegions += region to sampleSize
        return Bitmap.createBitmap(region.width / sampleSize, region.height / sampleSize, Bitmap.Config.ARGB_8888)
    }
    override suspend fun setSystemWallpaper(bitmap: Bitmap, includeLock: Boolean): Int {
        if (!setSucceeds) return NO_WALLPAPER_ID
        setCount++
        setIncludeLock = includeLock
        systemId++
        if (includeLock) lockId = systemId + 100
        return systemId
    }
    override suspend fun save(bitmap: Bitmap): Boolean {
        saved = bitmap
        return true
    }
}

internal class FakeDailyPhotos(folder: File, copies: WallpaperCopyRepository) : DailyPhotoRepository(
    api = object : DailyPhotoApi {
        override suspend fun feedJson(source: DailyPhotoSource, country: String): String? = null
        override suspend fun download(url: String, maxBytes: Int): ByteArray? = null
    },
    cache = DailyPhotoCache(folder, dailyTestClock),
    wallpaperCopyRepository = copies,
    ioDispatcher = Dispatchers.Unconfined,
) {
    val feeds = mutableMapOf<DailyPhotoSource, DailyPhotoFeed>()
    val originals = mutableMapOf<String, ByteArray>()
    var feedRequests = 0
    val downloads = mutableListOf<String>()

    override suspend fun photos(source: DailyPhotoSource): DailyPhotoFeed {
        feedRequests++
        return feeds[source] ?: DailyPhotoFeed.Unavailable
    }
    override suspend fun originalBytes(photo: DailyPhoto): ByteArray? {
        downloads += photo.imageUrl
        return originals[photo.imageUrl]
    }
}

internal class FakeConditions : DeviceConditionsRepository(RuntimeEnvironment.getApplication()) {
    var network = NetworkConditions(connected = true, unmetered = true, roaming = false)
    var powerSave = false
    var screenOn = true
    val screenOff = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun network() = network
    override fun isPowerSaveMode() = powerSave
    override fun isScreenOn() = screenOn
    override fun screenOffEvents(): Flow<Unit> = screenOff
}
