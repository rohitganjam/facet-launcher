package com.facetlauncher.app.data.dailyphotos

import com.facetlauncher.app.data.model.DailyPhoto
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** Bing's `HPImageArchive` answer; every field the app does not use is ignored. */
@Serializable
private data class BingResponse(val images: List<BingImage>? = null)

@Serializable
private data class BingImage(
    val startdate: String? = null,
    val urlbase: String? = null,
    val title: String? = null,
    val copyright: String? = null,
)

private val bingJson = Json { ignoreUnknownKeys = true }

/**
 * The photos in Bing's `HPImageArchive` JSON, or `null` when it is not that. A photo's addresses are built from its `urlbase` (a 640 px preview
 * and the UHD original) and must pass [isAllowedImageUrl], or it is left out. Bing's copyright reads "<caption> (© <credit>)": the caption is the
 * title and the credit is what is in the brackets, and a copyright not in that form is the credit with Bing's own short title.
 */
internal fun parseBingFeed(json: String): List<DailyPhoto>? {
    val images = runCatching { bingJson.decodeFromString<BingResponse>(json) }.getOrNull()?.images ?: return null
    return images.mapNotNull { image ->
        val base = image.urlbase?.takeIf { it.startsWith("/") } ?: return@mapNotNull null
        val thumb = "https://$BING_HOST${base}_640x480.jpg".takeIf(::isAllowedImageUrl) ?: return@mapNotNull null
        val original = "https://$BING_HOST${base}_UHD.jpg".takeIf(::isAllowedImageUrl) ?: return@mapNotNull null
        val (title, credit) = splitCopyright(image.copyright.orEmpty(), image.title.orEmpty())
        DailyPhoto(title = title, credit = credit, thumbUrl = thumb, imageUrl = original, date = bingDate(image.startdate))
    }
}

private fun splitCopyright(copyright: String, fallbackTitle: String): Pair<String, String> {
    val open = copyright.lastIndexOf("(©")
    if (open <= 0 || !copyright.endsWith(")")) return fallbackTitle to copyright
    return copyright.substring(0, open).trim() to copyright.substring(open + 1, copyright.length - 1).trim()
}

/** `20261119` as `2026-11-19`; `null` for anything else. */
private fun bingDate(startDate: String?): String? =
    startDate?.takeIf { it.length == 8 && it.all(Char::isDigit) }?.let { "${it.substring(0, 4)}-${it.substring(4, 6)}-${it.substring(6)}" }
