package com.facetlauncher.app.data.weather

import com.facetlauncher.app.BuildConfig
import com.facetlauncher.app.data.di.IoDispatcher
import com.facetlauncher.app.data.model.WeatherForecast
import com.facetlauncher.app.data.model.WeatherPlace
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

enum class WeatherFailure {
    /** HTTP 429: over MET's limit. The caller fails and tries again later. */
    RATE_LIMITED,

    /** HTTP 403: MET refuses this `User-Agent`. */
    BLOCKED,
    OFFLINE,
    SERVER,
    INVALID_RESPONSE,
}

sealed interface WeatherApiResult {
    data class Success(val forecast: WeatherForecast, val expiresAt: Instant?, val lastModified: String?) : WeatherApiResult
    data class NotModified(val expiresAt: Instant?) : WeatherApiResult

    /** [retryAt] is from `Retry-After` when the server sent one. */
    data class Failed(val reason: WeatherFailure, val retryAt: Instant? = null) : WeatherApiResult
}

/** The forecast service. A single call; no retries and no caching, which belong to [WeatherRepository]. */
interface WeatherApi {
    suspend fun fetch(place: WeatherPlace, ifModifiedSince: String?): WeatherApiResult
}

/**
 * MET Norway Locationforecast 2.0 `compact`. Identifies itself with a `User-Agent` as MET requires, sends the
 * coordinates rounded to two decimals (MET accepts four), and uses a conditional request when it has a
 * `Last-Modified` from the previous answer. The only code that opens a network connection in the app.
 */
@Singleton
class MetWeatherApi @Inject constructor(
    private val clock: Clock,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : WeatherApi {

    override suspend fun fetch(place: WeatherPlace, ifModifiedSince: String?): WeatherApiResult = withContext(ioDispatcher) {
        val connection = try {
            URL(requestUrl(place)).openConnection() as HttpURLConnection
        } catch (_: IOException) {
            return@withContext WeatherApiResult.Failed(WeatherFailure.OFFLINE)
        }
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("User-Agent", USER_AGENT)
            ifModifiedSince?.let { connection.setRequestProperty("If-Modified-Since", it) }
            interpret(connection.responseCode, connection) { connection.inputStream.bufferedReader().use { it.readText() } }
        } catch (_: IOException) {
            WeatherApiResult.Failed(WeatherFailure.OFFLINE)
        } finally {
            connection.disconnect()
        }
    }

    private fun interpret(code: Int, connection: HttpURLConnection, body: () -> String): WeatherApiResult {
        val now = clock.instant()
        val expiresAt = parseHttpDate(connection.getHeaderField("Expires"))
        return when {
            code == HttpURLConnection.HTTP_OK -> {
                val forecast = parseMetForecast(body())
                    ?: return WeatherApiResult.Failed(WeatherFailure.INVALID_RESPONSE)
                WeatherApiResult.Success(forecast, expiresAt, connection.getHeaderField("Last-Modified"))
            }
            code == HttpURLConnection.HTTP_NOT_MODIFIED -> WeatherApiResult.NotModified(expiresAt)
            code == HTTP_TOO_MANY_REQUESTS ->
                WeatherApiResult.Failed(WeatherFailure.RATE_LIMITED, parseRetryAfter(connection.getHeaderField("Retry-After"), now))
            code == HttpURLConnection.HTTP_FORBIDDEN -> WeatherApiResult.Failed(WeatherFailure.BLOCKED)
            else -> WeatherApiResult.Failed(WeatherFailure.SERVER)
        }
    }

    internal companion object {
        const val HTTP_TOO_MANY_REQUESTS = 429
        private const val TIMEOUT_MS = 15_000
        private const val BASE_URL = "https://api.met.no/weatherapi/locationforecast/2.0/compact"

        // MET asks for an app name and a way to reach the developer.
        val USER_AGENT = "FacetLauncher/${BuildConfig.VERSION_NAME} github.com/rohitganjam/facet-launcher"

        fun requestUrl(place: WeatherPlace): String {
            val altitude = place.altitudeM?.let { "&altitude=$it" }.orEmpty()
            return String.format(Locale.ROOT, "%s?lat=%.2f&lon=%.2f%s", BASE_URL, place.latitude, place.longitude, altitude)
        }

        fun parseHttpDate(value: String?): Instant? = try {
            value?.let { ZonedDateTime.parse(it, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant() }
        } catch (_: DateTimeParseException) {
            null
        }

        /** `Retry-After` is either whole seconds or an HTTP date. */
        fun parseRetryAfter(value: String?, now: Instant): Instant? {
            val seconds = value?.trim()?.toLongOrNull()
            return if (seconds != null) now.plus(Duration.ofSeconds(seconds)) else parseHttpDate(value)
        }
    }
}
