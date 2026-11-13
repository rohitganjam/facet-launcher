package com.facetlauncher.app.data.dailyphotos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.DailyPhoto
import com.facetlauncher.app.data.model.DailyPhotoSource
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** What a feed gave: its photos ([savedEarlier] when the refresh failed and these are from an earlier day), or nothing at all. */
sealed interface DailyPhotoFeed {
    data class Photos(val photos: List<DailyPhoto>, val savedEarlier: Boolean) : DailyPhotoFeed
    data object Unavailable : DailyPhotoFeed
}

/**
 * The daily photos. A feed and its thumbnails are fetched at most once per calendar day and kept on disk ([DailyPhotoCache]); an original is
 * downloaded only when a photo is picked, and not kept. `open` so tests can substitute fixed values.
 */
@Singleton
open class DailyPhotoRepository @Inject constructor(
    private val api: DailyPhotoApi,
    private val cache: DailyPhotoCache,
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val thumbnails = LruCache<String, Bitmap>(THUMBNAILS_IN_MEMORY)

    /** [source]'s photos for the phone's country: today's cache when there is one, otherwise a fetch, otherwise the last cache. */
    open suspend fun photos(source: DailyPhotoSource): DailyPhotoFeed = photos(source, peapixCountry(Locale.getDefault()))

    internal suspend fun photos(source: DailyPhotoSource, country: String): DailyPhotoFeed = withContext(ioDispatcher) {
        val key = if (source.usesCountry) country else ""
        val saved = cache.readFeed(source, key)
        val savedPhotos = saved?.let { parsePeapixFeed(it.json) }?.takeIf { it.isNotEmpty() }
        if (saved != null && savedPhotos != null && saved.day == cache.today()) return@withContext DailyPhotoFeed.Photos(savedPhotos, savedEarlier = false)
        val fetched = api.feedJson(source, country)?.let { json -> parsePeapixFeed(json)?.takeIf { it.isNotEmpty() }?.let { json to it } }
        when {
            fetched != null -> {
                cache.writeFeed(source, key, fetched.first, fetched.second.map { it.thumbUrl })
                DailyPhotoFeed.Photos(fetched.second, savedEarlier = false)
            }
            savedPhotos != null -> DailyPhotoFeed.Photos(savedPhotos, savedEarlier = true)
            else -> DailyPhotoFeed.Unavailable
        }
    }

    /** The preview of [photo]: from memory, else the day's cache, else downloaded and cached; `null` when it cannot be had. */
    open suspend fun thumbnail(source: DailyPhotoSource, photo: DailyPhoto): Bitmap? = withContext(ioDispatcher) {
        thumbnails.get(photo.thumbUrl) ?: run {
            val bytes = cache.readThumbnail(source, photo.thumbUrl)
                ?: api.download(photo.thumbUrl, MAX_THUMBNAIL_BYTES)?.also { cache.writeThumbnail(source, photo.thumbUrl, it) }
            bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }?.also { thumbnails.put(photo.thumbUrl, it) }
        }
    }

    /** The original of [photo], upright and shrunk to [maxSide] on its long side; `null` when it cannot be downloaded or read. */
    open suspend fun original(photo: DailyPhoto, maxSide: Int): Bitmap? {
        val bytes = api.download(photo.imageUrl, MAX_ORIGINAL_BYTES) ?: return null
        return wallpaperCopyRepository.decodeBytes(bytes, maxSide)
    }

    private companion object {
        const val THUMBNAILS_IN_MEMORY = 16
        const val MAX_THUMBNAIL_BYTES = 1 * 1024 * 1024
        const val MAX_ORIGINAL_BYTES = 25 * 1024 * 1024
    }
}
