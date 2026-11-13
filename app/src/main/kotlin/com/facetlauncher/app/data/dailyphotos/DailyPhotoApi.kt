package com.facetlauncher.app.data.dailyphotos

import com.facetlauncher.app.BuildConfig
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.DailyPhotoSource
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** The daily photo service: single calls, no caching and no retries, which belong to [DailyPhotoRepository]. `null` is any failure. */
interface DailyPhotoApi {
    /** The feed's JSON, as served. */
    suspend fun feedJson(source: DailyPhotoSource, country: String): String?

    /** The bytes at [url] (an address [isAllowedImageUrl] accepts), or `null` when it fails or is larger than [maxBytes]. */
    suspend fun download(url: String, maxBytes: Int): ByteArray?
}

/**
 * Peapix's feeds, which carry Bing's photo of the day and Windows Spotlight. With [MetWeatherApi][com.facetlauncher.app.data.weather.MetWeatherApi]
 * the only code that opens a network connection in the app, and only when the user opens Daily photos or picks a photo.
 */
@Singleton
class PeapixApi @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : DailyPhotoApi {

    override suspend fun feedJson(source: DailyPhotoSource, country: String): String? = request(feedUrl(source, country), MAX_FEED_BYTES)?.toString(Charsets.UTF_8)

    override suspend fun download(url: String, maxBytes: Int): ByteArray? = if (isAllowedImageUrl(url)) request(url, maxBytes) else null

    private suspend fun request(address: String, maxBytes: Int): ByteArray? = withContext(ioDispatcher) {
        runCatching {
            val connection = URL(address).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                connection.setRequestProperty("User-Agent", USER_AGENT)
                if (connection.responseCode != HttpURLConnection.HTTP_OK || connection.contentLength > maxBytes) return@runCatching null
                connection.inputStream.use { readAtMost(it, maxBytes) }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    /** All of [input], or `null` when it is longer than [maxBytes] (it stops reading as soon as that is known). */
    private fun readAtMost(input: java.io.InputStream, maxBytes: Int): ByteArray? {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_BYTES)
        var total = 0
        var read = input.read(buffer)
        while (read >= 0 && total <= maxBytes) {
            out.write(buffer, 0, read)
            total += read
            read = input.read(buffer)
        }
        return out.toByteArray().takeIf { total <= maxBytes }
    }

    internal companion object {
        private const val TIMEOUT_MS = 15_000
        private const val BUFFER_BYTES = 16 * 1024
        private const val MAX_FEED_BYTES = 256 * 1024
        private const val BASE_URL = "https://peapix.com/"

        /** The feeds hold a week; asking for more returns the same seven. */
        const val FEED_COUNT = 7

        val USER_AGENT = "FacetLauncher/${BuildConfig.VERSION_NAME} github.com/rohitganjam/facet-launcher"

        fun feedUrl(source: DailyPhotoSource, country: String): String =
            BASE_URL + source.feedPath + "?n=$FEED_COUNT" + if (source.usesCountry) "&country=$country" else ""
    }
}
