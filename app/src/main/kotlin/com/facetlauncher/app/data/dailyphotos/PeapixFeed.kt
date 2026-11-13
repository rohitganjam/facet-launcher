package com.facetlauncher.app.data.dailyphotos

import com.facetlauncher.app.data.model.DailyPhoto
import java.net.URI
import java.util.Locale
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** The countries Peapix's Bing feed has its own set for; anywhere else gets [DEFAULT_COUNTRY]. */
private val PEAPIX_COUNTRIES = setOf("au", "br", "ca", "cn", "de", "fr", "in", "it", "jp", "es", "gb", "us")

internal const val DEFAULT_COUNTRY = "us"

/** The Bing feed's `country` for [locale]: its own region when Peapix has one, otherwise [DEFAULT_COUNTRY]. */
internal fun peapixCountry(locale: Locale): String = locale.country.lowercase(Locale.ROOT).takeIf { it in PEAPIX_COUNTRIES } ?: DEFAULT_COUNTRY

/** One entry of a Peapix feed; every field the app does not use is ignored. */
@Serializable
private data class PeapixItem(
    val title: String? = null,
    val copyright: String? = null,
    val thumbUrl: String? = null,
    val imageUrl: String? = null,
    val date: String? = null,
)

private val feedJson = Json { ignoreUnknownKeys = true }

/**
 * The photos in a feed's JSON, or `null` when it is not a feed. An entry without both image addresses, or whose addresses are not allowed
 * ([isAllowedImageUrl]), is left out; the credit and title default to empty text.
 */
internal fun parsePeapixFeed(json: String): List<DailyPhoto>? {
    val items = runCatching { feedJson.decodeFromString<List<PeapixItem>>(json) }.getOrNull() ?: return null
    return items.mapNotNull { item ->
        val thumb = item.thumbUrl?.takeIf(::isAllowedImageUrl) ?: return@mapNotNull null
        val image = item.imageUrl?.takeIf(::isAllowedImageUrl) ?: return@mapNotNull null
        DailyPhoto(title = item.title.orEmpty(), credit = item.copyright.orEmpty(), thumbUrl = thumb, imageUrl = image, date = item.date)
    }
}

/** Only https, and only Peapix's own hosts: a feed cannot send the app to fetch from anywhere else. */
internal fun isAllowedImageUrl(url: String): Boolean {
    val uri = runCatching { URI(url) }.getOrNull() ?: return false
    val host = uri.host?.lowercase(Locale.ROOT) ?: return false
    return uri.scheme.equals("https", ignoreCase = true) && (host == PEAPIX_HOST || host.endsWith(".$PEAPIX_HOST"))
}

private const val PEAPIX_HOST = "peapix.com"
