package com.facetlauncher.app.data.dailyphotos

import com.facetlauncher.app.BuildConfig
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.DailyPhotoSource
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
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
 * Bing's photo of the day and Windows Spotlight, both from Microsoft's own services. With [MetWeatherApi][com.facetlauncher.app.data.weather.MetWeatherApi]
 * the only code that opens a network connection in the app, and only when the user opens Daily photos or picks a photo.
 */
@Singleton
class HttpDailyPhotoApi @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : DailyPhotoApi {

    override suspend fun feedJson(source: DailyPhotoSource, country: String): String? = withBrowserAgentFallback(USER_AGENT) { agent ->
        when (source) {
            DailyPhotoSource.BING -> fetchText(bingFeedUrl(country, Locale.getDefault()), agent)?.takeIf { parseBingFeed(it)?.isNotEmpty() == true }
            DailyPhotoSource.SPOTLIGHT ->
                fetchSpotlight(spotlightCandidates(country, Locale.getDefault()), SPOTLIGHT_CALLS, SPOTLIGHT_ENOUGH) { c, l -> fetchText(spotlightUrl(c, l), agent) }
        }
    }

    private suspend fun fetchText(address: String, userAgent: String): String? = request(address, MAX_FEED_BYTES, userAgent)?.toString(Charsets.UTF_8)

    override suspend fun download(url: String, maxBytes: Int): ByteArray? = if (isAllowedImageUrl(url)) request(url, maxBytes, USER_AGENT) else null

    private suspend fun request(address: String, maxBytes: Int, userAgent: String): ByteArray? = withContext(ioDispatcher) {
        runCatching {
            val connection = URL(address).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                connection.setRequestProperty("User-Agent", userAgent)
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
        private const val BING_URL = "https://$BING_HOST/HPImageArchive.aspx"
        private const val SPOTLIGHT_URL = "https://fd.api.iris.microsoft.com/v4/api/selection?placement=88000820&bcnt=4&fmt=json"

        /** Spotlight answers a random 4 each time, so a few calls make a fuller set: asking stops at [SPOTLIGHT_ENOUGH] different photos, or after this many calls. */
        private const val SPOTLIGHT_CALLS = 3
        private const val SPOTLIGHT_ENOUGH = 8

        /** The days of Bing's archive asked for. */
        const val FEED_COUNT = 7

        val USER_AGENT = "FacetLauncher/${BuildConfig.VERSION_NAME} github.com/rohitganjam/facet-launcher"

        fun spotlightUrl(country: String, locale: String): String = "$SPOTLIGHT_URL&country=$country&locale=$locale"

        fun bingFeedUrl(country: String, phone: Locale): String = "$BING_URL?format=js&idx=0&n=$FEED_COUNT&mkt=${localeTag(country, phone)}"
    }
}
