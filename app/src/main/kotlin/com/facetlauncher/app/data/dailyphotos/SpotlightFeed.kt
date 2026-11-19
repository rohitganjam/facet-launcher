package com.facetlauncher.app.data.dailyphotos

import com.facetlauncher.app.data.model.DailyPhoto
import java.util.Locale
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
private data class SpotlightResponse(val batchrsp: SpotlightBatch? = null)

@Serializable
private data class SpotlightBatch(val items: List<SpotlightItem> = emptyList())

/** Microsoft sends each item's content as a JSON string inside the item. */
@Serializable
private data class SpotlightItem(val item: String? = null)

@Serializable
private data class SpotlightEnvelope(val ad: SpotlightAd? = null)

@Serializable
private data class SpotlightAd(
    val title: String? = null,
    val description: String? = null,
    val copyright: String? = null,
    val iconHoverText: String? = null,
    val landscapeImage: SpotlightImage? = null,
)

@Serializable
private data class SpotlightImage(val asset: String? = null)

private val spotlightJson = Json { ignoreUnknownKeys = true }

/**
 * The photos in Windows Spotlight's JSON (Microsoft's `batchrsp`, or several of them merged by [mergeSpotlightResponses]), or `null` when it is not
 * that. The landscape image is both the preview and the original. A missing title is the first line of the hover text, else the description, and a
 * missing credit the hover text's © line. An entry without an allowed address is left out, and so is a repeat.
 */
internal fun parseSpotlightFeed(json: String): List<DailyPhoto>? {
    val items = runCatching { spotlightJson.decodeFromString<SpotlightResponse>(json) }.getOrNull()?.batchrsp?.items ?: return null
    return items.mapNotNull { entry ->
        val ad = entry.item?.let { runCatching { spotlightJson.decodeFromString<SpotlightEnvelope>(it) }.getOrNull() }?.ad ?: return@mapNotNull null
        val image = ad.landscapeImage?.asset?.takeIf(::isAllowedImageUrl) ?: return@mapNotNull null
        val hover = ad.iconHoverText.orEmpty().lines().map(String::trim).filter(String::isNotEmpty)
        val title = ad.title?.trim().takeUnless { it.isNullOrEmpty() }
            ?: hover.firstOrNull()?.takeUnless { it.startsWith("©") || it.contains("right-click", ignoreCase = true) || it.contains("learn more", ignoreCase = true) }
            ?: ad.description?.trim().orEmpty()
        val credit = ad.copyright?.trim().takeUnless { it.isNullOrEmpty() } ?: hover.firstOrNull { it.startsWith("©") }.orEmpty()
        DailyPhoto(title = title, credit = credit, thumbUrl = image, imageUrl = image)
    }.distinctBy { it.imageUrl }
}

/** Joins the `items` of several Spotlight responses into one response; `null` when none is one. */
internal fun mergeSpotlightResponses(responses: List<String>): String? {
    val items = responses.mapNotNull { runCatching { spotlightJson.decodeFromString<SpotlightResponse>(it) }.getOrNull()?.batchrsp?.items }.flatten()
    if (items.isEmpty()) return null
    return spotlightJson.encodeToString(SpotlightResponse.serializer(), SpotlightResponse(SpotlightBatch(items)))
}

/**
 * What Microsoft is asked, in order, until one answers with photos: the phone's own country and language, then English for the same country (an
 * unknown language is refused, an unknown country is not), then English for the US.
 */
internal fun spotlightCandidates(country: String, phone: Locale): List<Pair<String, String>> {
    val upper = country.uppercase(Locale.ROOT)
    return listOf(upper to localeTag(country, phone), upper to "en-$upper", "US" to "en-US").distinct()
}

/**
 * The merged JSON of up to [calls] answers (each a random few photos) for the first of [candidates] that has photos, or `null` when none has. Only
 * the first answer decides whether a candidate works; asking stops once the merged answers hold [enough] different photos, and a later answer that
 * fails is skipped.
 */
internal suspend fun fetchSpotlight(
    candidates: List<Pair<String, String>>,
    calls: Int,
    enough: Int,
    fetch: suspend (country: String, locale: String) -> String?,
): String? {
    for ((country, locale) in candidates) {
        val answers = mutableListOf(fetch(country, locale)?.takeIf { parseSpotlightFeed(it)?.isNotEmpty() == true } ?: continue)
        var asked = 1
        while (asked < calls && (parseSpotlightFeed(mergeSpotlightResponses(answers).orEmpty())?.size ?: 0) < enough) {
            asked++
            fetch(country, locale)?.let(answers::add)
        }
        return mergeSpotlightResponses(answers)
    }
    return null
}
