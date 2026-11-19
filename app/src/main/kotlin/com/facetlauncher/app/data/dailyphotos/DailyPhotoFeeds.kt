package com.facetlauncher.app.data.dailyphotos

import com.facetlauncher.app.data.model.DailyPhoto
import com.facetlauncher.app.data.model.DailyPhotoSource
import java.net.URI
import java.util.Locale

internal const val DEFAULT_COUNTRY = "us"

/** The `country` both feeds are asked for: the phone's own region, or [DEFAULT_COUNTRY] when it has none. Microsoft does not check it. */
internal fun feedCountry(locale: Locale): String = locale.country.lowercase(Locale.ROOT).ifEmpty { DEFAULT_COUNTRY }

/** The `locale` / `mkt` sent with [country]: the phone's language tag when its own country is [country], otherwise English for the US. */
internal fun localeTag(country: String, phone: Locale): String =
    if (phone.language.isNotEmpty() && phone.country.equals(country, ignoreCase = true)) "${phone.language}-${country.uppercase(Locale.ROOT)}" else "en-US"

/** A common browser's User-Agent, used only to ask a feed again when it gave nothing with the app's own: Microsoft's Spotlight is said to refuse unfamiliar ones. */
internal const val BROWSER_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"

/** What [attempt] gives with the app's own [ownAgent], or when that is `null`, what it gives with [BROWSER_USER_AGENT]. */
internal suspend fun <T : Any> withBrowserAgentFallback(ownAgent: String, attempt: suspend (userAgent: String) -> T?): T? =
    attempt(ownAgent) ?: attempt(BROWSER_USER_AGENT)

/** The photos in [source]'s feed JSON, or `null` when it is not a feed; see [parseBingFeed] and [parseSpotlightFeed]. */
internal fun parseFeed(source: DailyPhotoSource, json: String): List<DailyPhoto>? = when (source) {
    DailyPhotoSource.BING -> parseBingFeed(json)
    DailyPhotoSource.SPOTLIGHT -> parseSpotlightFeed(json)
}

/** Only https, and only the photo hosts of Bing and Microsoft's Spotlight: a feed cannot send the app to fetch from anywhere else. */
internal fun isAllowedImageUrl(url: String): Boolean {
    val uri = runCatching { URI(url) }.getOrNull() ?: return false
    val host = uri.host?.lowercase(Locale.ROOT) ?: return false
    return uri.scheme.equals("https", ignoreCase = true) && host in ALLOWED_IMAGE_HOSTS
}

internal const val BING_HOST = "www.bing.com"
private val ALLOWED_IMAGE_HOSTS = setOf(BING_HOST, "res.public.onecdn.static.microsoft")
